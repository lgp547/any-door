package io.github.lgp547.anydoorplugin.dialog.components;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PsiCodeFragmentImpl;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBDimension;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import io.github.lgp547.anydoorplugin.dialog.components.intellij.MyCodeFragmentInputComponent;
import io.github.lgp547.anydoorplugin.dialog.utils.JavaFileInfoUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.debugger.JavaDebuggerEditorsProvider;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 参考 XDebuggerEvaluationDialog
 */
public class PreRunDialogWrapper extends DialogWrapper {
    private final Project project;

    public MyCodeFragmentInputComponent component;
    private final JavaFileInfoUtil.JavaFileInfo javaFileInfo;

    public PreRunDialogWrapper(Project project, JavaFileInfoUtil.JavaFileInfo javaFileInfo) {
        super(project, true, IdeModalityType.MODELESS);
        this.project = project;
        this.javaFileInfo = javaFileInfo;

        setTitle("Edit AnyDoorInjectedClass Java File");
        setOKButtonText("Save");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {

        // 内容
        XExpression text = new XExpressionImpl(javaFileInfo.getContent(), JavaLanguage.INSTANCE, null, EvaluationMode.CODE_FRAGMENT);
        XDebuggerEditorsProvider myEditorsProvider = new JavaDebuggerEditorsProvider();
        XSourcePosition mySourcePosition = null;

        component = new MyCodeFragmentInputComponent(project, myEditorsProvider,
                mySourcePosition, text, this.getDimensionServiceKey() + ".splitter", this.myDisposable);

        component.getInputEditor().getEditorComponent().setPreferredSize(new JBDimension(470, 350));

        // 导入类
        PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(component.getDocument());
        if (file instanceof PsiCodeFragmentImpl fileImpl) {
            for (String importStr : javaFileInfo.getImportStrs()) {
                // todo: 若存在*结束的？
                fileImpl.addImportsFromString(importStr);
            }
        }

        return component.getMainComponent();
    }

    @Override
    protected void doOKAction() {
        saveChangesToFile();
        super.doOKAction();
    }

    private void saveChangesToFile() {
        PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(component.getDocument());
        if (file instanceof PsiCodeFragmentImpl fileImpl) {
            String importsToString = fileImpl.importsToString();
            List<String> imports = Arrays.stream(importsToString.split(",")).collect(Collectors.toList());
            doUpdateFile(JavaFileInfoUtil.toAnyDoorInjectedClassStr(imports, component.getDocument().getText()));
        }
    }

    private void doUpdateFile(String string) {
        // 写文件
        WriteCommandAction.runWriteCommandAction(project, "AnyDoorUpdateFile", "AnyDoorInjectedClass",  () -> {
            try {
                FileUtil.writeToFile(new File(javaFileInfo.getFilePath()), string);
            } catch (IOException e) {
                System.err.println("Error writing file: " + e.getMessage());
            }
        });
    }
}