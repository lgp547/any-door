package io.github.lgp547.anydoorplugin.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiParameterList;
import io.github.lgp547.anydoorplugin.dto.ParamCacheDto;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TextAreaDialog extends DialogWrapper {

    private final JSONEditor textArea;
    private final ContentPanel contentPanel;

    private Runnable okAction;

    public TextAreaDialog(Project project, String title, PsiParameterList psiParameterList, ParamCacheDto paramCacheDto) {
        super(project, true, IdeModalityType.MODELESS);
        setTitle(title);
        textArea = new JSONEditor(paramCacheDto.getContent(), psiParameterList, project);
        contentPanel = new ContentPanel(textArea);
        contentPanel.addCacheButtonListener(e -> textArea.genCacheContent());
        contentPanel.addSimpleButtonListener(e -> textArea.genSimpleContent());
        contentPanel.addJsonButtonListener(e -> textArea.genJsonContent());
        contentPanel.addJsonToQueryButtonListener(e -> textArea.jsonToQuery());
        contentPanel.addQueryToJsonButtonListener(e -> textArea.queryToJson());
        contentPanel.initRunNum(paramCacheDto.getRunNum());
        contentPanel.initIsConcurrent(paramCacheDto.getConcurrent());

        init();
    }

    public void setOkAction(Runnable runnable) {
        okAction = runnable;
    }

    public String getText() {
        return textArea.getText();
    }

    public Long getRunNum() {
        return contentPanel.getRunNum();
    }

    public Boolean getIsConcurrent() {
        return contentPanel.getIsConcurrent();
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

}
