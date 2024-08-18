package io.github.lgp547.anydoorplugin.dialog.components;

import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.json.JsonFileType;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.JBTextArea;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.intellij.xdebugger.impl.evaluate.CodeFragmentInputComponent;
import com.intellij.xdebugger.impl.ui.DebuggerUIUtil;
import io.github.lgp547.anydoorplugin.action.AnyDoorOpenAction;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class MyDialogWrapper extends DialogWrapper {
    private final Project project;

    public MyDialogWrapper(Project project, VirtualFile virtualFile) {
        super(project, true, IdeModalityType.MODELESS);
        this.project = project;
//        this.virtualFile = virtualFile;
        setTitle("Edit Java File");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        // XDebuggerEvaluationDialog

        XExpression text = new XExpressionImpl("", JavaLanguage.INSTANCE, null);
        XDebugSession session = AnyDoorOpenAction.session;
        XDebuggerEditorsProvider myEditorsProvider = session.getDebugProcess().getEditorsProvider();
        XStackFrame currentStackFrame = session.getCurrentStackFrame();
        XSourcePosition mySourcePosition = currentStackFrame == null ? null : currentStackFrame.getSourcePosition();

        // AnyDoorInjectedClass
        CodeFragmentInputComponent component = new CodeFragmentInputComponent(project, myEditorsProvider,
                mySourcePosition, text, this.getDimensionServiceKey() + ".splitter", this.myDisposable);

        return component.getMainComponent();
    }

    private void saveChangesToFile() {
//        Document doc = FileDocumentManager.getInstance().getDocument(virtualFile);
//        if (doc != null) {
//            String updatedText = textArea.getText();
//            WriteCommandAction.runWriteCommandAction(project, () -> {
//                doc.setText(updatedText);
//            });
//        }
    }

    @Override
    protected void doOKAction() {
        // Optionally save changes when OK is pressed
        saveChangesToFile();
        super.doOKAction();
    }


}