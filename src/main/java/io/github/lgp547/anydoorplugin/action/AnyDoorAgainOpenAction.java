package io.github.lgp547.anydoorplugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 响应 Again Open AnyDoor 入口
 */
public class AnyDoorAgainOpenAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (null == project) {
            throw new IllegalArgumentException("idea arg error (project is null)");
        }
        PsiMethod psiMethod = AnyDoorOpenAction.getPreMethod(project.getName());
        if (null != psiMethod) {
            try {
                new AnyDoorPerformed().invoke(project, psiMethod);
            } catch (Exception exception) {
                NotifierUtil.notifyError(project, "invoke exception " + exception.getMessage());
            }
        }
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }
}
