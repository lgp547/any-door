package io.github.lgp547.anydoorplugin.action;

import java.nio.file.Path;
import java.util.Vector;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.DumbAware;
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
public class ParamListMenuFactory implements ToolWindowFactory, DumbAware {
    private final ParamListUI ui = new ParamListUI();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Vector<Vector<Object>> vectors = new Vector<>();
        Vector<Object> v1 = new Vector<>();
        v1.add(true);
        v1.add(1);
        v1.add("bb");
        v1.add("2021-06-17");
        Vector<Object> v2 = new Vector<>();
        v2.add(false);
        v2.add(2);
        v2.add("aa");
        v2.add("2021-06-17");
        vectors.add(v1);
        vectors.add(v2);
        ui.initDataList(vectors);
        Path pluginPath = PluginManagerCore.getPlugin(PluginId.getId("io.github.lgp547.any-door-plugin")).getPluginPath();
        System.out.println(pluginPath);
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
