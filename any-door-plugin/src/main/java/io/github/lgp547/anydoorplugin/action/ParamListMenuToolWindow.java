package io.github.lgp547.anydoorplugin.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import io.github.lgp547.anydoorplugin.dialog.ParamListUI;
import org.jetbrains.annotations.NotNull;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-16 11:03
 **/
public class ParamListMenuToolWindow implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ParamListUI ui = new ParamListUI(project);

        // 获取内容工厂的实例
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        // 获取 ToolWindow 显示的内容
        Content content = contentFactory.createContent(ui, "", false);
        // 设置 ToolWindow 显示的内容
        toolWindow.getContentManager().addContent(content);
        //初始化数据

        // 全局使用

    }
}
