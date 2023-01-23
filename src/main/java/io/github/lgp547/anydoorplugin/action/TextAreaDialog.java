package io.github.lgp547.anydoorplugin.action;

import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.LocalTimeCounter;
import com.intellij.util.ui.JBDimension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class TextAreaDialog extends DialogWrapper {
    private final ContentPanel contentPanel;

    private Runnable okAction;

    private final Project project;

    public TextAreaDialog(Project project, String title, String text) {
        super(project, true, IdeModalityType.MODELESS);
        this.project = project;
        setTitle(title);
        contentPanel = new ContentPanel(text, project);
        init();
    }

    public void setOkAction(Runnable runnable) {
        okAction = runnable;
    }

    public String getText() {
        return contentPanel.getText();
    }

    @Override
    protected void doOKAction() {
        okAction.run();
        super.doOKAction();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPanel;
    }

    private class ContentPanel extends JBPanel<ContentPanel> {
        final EditorTextField textArea;

        public String getText() {
            return textArea.getText();
        }

        public ContentPanel(String text, Project project) {
            super(new GridBagLayout());
            setPreferredSize(new JBDimension(600, 500));


//            GridBagConstraints constraints1 = new GridBagConstraints();
//            constraints1.anchor = GridBagConstraints.EAST;
//
//            JButton jButton = new JButton("gen json param");
//            jButton.addActionListener(e -> {
//                System.out.println("fill");
//            });
//            add(jButton, constraints1);


            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.gridx = 0;
            constraints.gridy = 1;

            textArea = new JSONEditor(text, project);
            textArea.requestFocusInWindow();
            textArea.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
//                    System.out.println("得到了");
                }

                @Override
                public void focusLost(FocusEvent e) {
                    textArea.requestFocusInWindow();
                }
            });
            add(textArea, constraints);
        }
    }

    private class JSONEditor extends EditorTextField {
        private final FileType fileType = JsonFileType.INSTANCE;

        public JSONEditor(String text, Project project) {
            super(text, project, JsonFileType.INSTANCE);
            super.setDocument(createDocument(text));
        }

        protected Document createDocument(String initText) {
            final PsiFileFactory factory = PsiFileFactory.getInstance(project);
            final long stamp = LocalTimeCounter.currentTime();
            final PsiFile psiFile = factory.createFileFromText("Dummy." + fileType.getDefaultExtension(), fileType, initText, stamp, true, false);

            //TextCompletionUtil.installProvider(psiFile, new MyTextFieldCompletionProvider(), true);
            return PsiDocumentManager.getInstance(project).getDocument(psiFile);
        }

        @Override
        protected @NotNull EditorEx createEditor() {
            final EditorEx ex = super.createEditor();

            ex.setHighlighter(HighlighterFactory.createHighlighter(project, JsonFileType.INSTANCE));
            ex.setEmbeddedIntoDialogWrapper(true);
            ex.setOneLineMode(false);

            return ex;
        }
    }

//    private static class MyTextFieldCompletionProvider extends TextFieldCompletionProvider implements DumbAware {
//        @Override
//        protected void addCompletionVariants(@NotNull String text, int offset, @NotNull String prefix, @NotNull CompletionResultSet result) {
//            result.addElement(LookupElementBuilder.create("123456"));
//            result.addElement(LookupElementBuilder.create("234567"));
//        }
//    }
}
