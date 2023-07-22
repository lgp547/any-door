package io.github.lgp547.anydoorplugin.dialog;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.TableView;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import io.github.lgp547.anydoorplugin.data.DataService;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.data.impl.ParamDataService;
import io.github.lgp547.anydoorplugin.dialog.components.CustomToolbar;
import io.github.lgp547.anydoorplugin.util.AnyDoorIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-16 20:35
 **/
public class ParamListUI extends JPanel {

    private final JBScrollPane contentPanel;
    private final JToolBar toolBar;

    private final ParamListTableModel tableModel;
    private final TableView<ViewData> table;

    private final Project project;
    private final DataService<ParamDataItem> dataService;

    private Data<ParamDataItem> data;

    public ParamListUI(Project project) {
        this.project = project;
        dataService = project.getService(ParamDataService.class);

        toolBar = new ParamListToolBar();
        tableModel = new ParamListTableModel();
        table = new ParamListTable(tableModel);
        contentPanel = new JBScrollPane(table);

        //布局
        paramListLayout();

        registerListener();

        initLoadData();
    }

    private void initLoadData() {
        VirtualFile[] files = FileEditorManager.getInstance(project).getSelectedFiles();
        if (files.length > 0) {
            VirtualFile file = files[0];
            readAndRefreshTable(file);
        }
    }

    private void registerListener() {
        registerFileEditorToRefreshDataList();
        registerTableDoubleClickToOpenAnyDoor();
    }

    private void registerTableDoubleClickToOpenAnyDoor() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow(); // 获取选中行的索引

                    ViewData value = tableModel.getRowValue(row);
                    String qName = value.dataItem.getQualifiedName();

                    int index = qName.lastIndexOf("#");
                    String className = qName.substring(0, index);
//                    String methodName = qName.substring(index + 1);

                    PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));

                    new MainUI(project, new DataContext(dataService, psiClass, value.dataItem.getQualifiedName(), data, value.dataItem)).show();
                }
            }
        });
    }

    private void registerFileEditorToRefreshDataList() {
        MessageBusConnection connection = project.getMessageBus().connect();

        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                readAndRefreshTable(event.getNewFile());
            }
        });
    }

    private void readAndRefreshTable(VirtualFile file) {
        if (Objects.nonNull(file)) {
            String qualifiedName = getQualifiedName(file);
            if (qualifiedName != null) {
                data = dataService.read(qualifiedName);
                tableModel.refreshAll(ViewData.toViewData(data));
            }
        }
    }

    private String getQualifiedName(VirtualFile file) {
        if (Objects.isNull(file)) {
            throw new IllegalArgumentException("file can not be null");
        }
        PsiManager psiManager = PsiManager.getInstance(project);
        PsiFile psiFile = psiManager.findFile(file);
        if (Objects.nonNull(psiFile) && psiFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            String packageName = psiJavaFile.getPackageName();
            if (!packageName.isEmpty()) {
                packageName += ".";
            }
            return packageName + psiJavaFile.getName().replace(".java", "");
        }
        return null;
    }

    private void paramListLayout() {
        GridBagLayout layout = new GridBagLayout();
        this.setLayout(layout);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(toolBar, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 0;
        gbc.gridheight = -1;
        this.add(contentPanel, gbc);
    }


    static class ParamListToolBar extends CustomToolbar {

        public ParamListToolBar() {
            super();
            this.setFloatable(false);

            this.addToolButton("新增", AnyDoorIcons.add_icon, AnyDoorIcons.add_hover_icon, e -> {
                System.out.println("新增");
            });
            this.addToolButton("删除", AnyDoorIcons.delete_icon, AnyDoorIcons.delete_hover_icon, e -> {
                System.out.println("删除");
            });
            this.addToolButton("刷新", AnyDoorIcons.refresh_icon, AnyDoorIcons.refresh_hover_icon, e -> {
                System.out.println("刷新");
            });
            this.addToolButton("查找", AnyDoorIcons.search_icon, AnyDoorIcons.search_icon, e -> {
                System.out.println("查找");
            });
        }

    }

    static class ParamListTableModel extends ListTableModel<ViewData> {

        public ParamListTableModel() {
            super(ViewData.COLUMN_NAMES);
        }

        public void refreshAll(List<ViewData> viewDataList) {
            setItems(viewDataList);
            this.fireTableDataChanged();
        }
    }

    static class ParamListTable extends TableView<ViewData> {

        public ParamListTable(ListTableModel<ViewData> model) {
            super(model);

            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            renderer.setHorizontalAlignment(JLabel.CENTER);
            this.getTableHeader().setDefaultRenderer(renderer);

        }
    }

    public static class ViewData {

        private boolean selected;
        private ParamDataItem dataItem;

        public ViewData(boolean selected, ParamDataItem dataItem) {
            this.selected = selected;
            this.dataItem = dataItem;
        }

        public ViewData(ParamDataItem item) {
            this(false, item);
        }

        public static List<ViewData> toViewData(Data<ParamDataItem> data) {
            if (Objects.isNull(data)) {
                return Collections.emptyList();
            }
            return data
                    .getDataList()
                    .stream()
                    .map(ViewData::new)
                    .collect(Collectors.toList());
        }

        static final ColumnInfo<ViewData, ?>[] COLUMN_NAMES = new ColumnInfo[]{
                new ColumnInfo<ViewData, Boolean>("") {
                    @Nullable
                    @Override
                    public Boolean valueOf(ViewData viewData) {
                        return viewData.selected;
                    }

                    @Override
                    public boolean isCellEditable(ViewData viewData) {
                        return true;
                    }

                    @Override
                    public void setValue(ViewData viewData, Boolean value) {
                        viewData.selected = value;
                    }

                    @Override
                    public @Nullable TableCellRenderer getRenderer(ViewData viewData) {
                        return (table, value, isSelected, hasFocus, row, column) -> {
                            JCheckBox checkBox = new JCheckBox();
                            checkBox.setContentAreaFilled(false);
                            checkBox.setOpaque(false);
                            checkBox.setBorderPainted(false);
                            checkBox.setFocusPainted(false);
                            checkBox.setSelected((Boolean) value);
                            checkBox.setHorizontalAlignment(JLabel.CENTER);
                            return checkBox;
                        };
                    }

                    @Override
                    public @Nullable TableCellEditor getEditor(ViewData viewData) {
                        return new DefaultCellEditor(new JCheckBox());
                    }

                    @Override
                    public int getWidth(JTable table) {
                        return 30;
                    }
                },
                new ColumnInfo<ViewData, Long>("ID") {
                    @Nullable
                    @Override
                    public Long valueOf(ViewData viewData) {
                        return viewData.dataItem.getId();
                    }

                    @Override
                    public void setValue(ViewData viewData, Long value) {
                        viewData.dataItem.setId(value);
                    }

                    @Override
                    public int getWidth(JTable table) {
                        return 30;
                    }

                    @Override
                    public @Nullable TableCellRenderer getRenderer(ViewData viewData) {
                        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                        renderer.setHorizontalAlignment(JLabel.CENTER);
                        return renderer;
                    }
                },
                new ColumnInfo<ViewData, String>("名称") {
                    @Nullable
                    @Override
                    public String valueOf(ViewData viewData) {
                        return viewData.dataItem.getName();
                    }

                    @Override
                    public void setValue(ViewData viewData, String value) {
                        viewData.dataItem.setName(value);
                    }

                    @Override
                    public int getWidth(JTable table) {
                        return 100;
                    }

                    @Override
                    public @Nullable TableCellRenderer getRenderer(ViewData viewData) {
                        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                        renderer.setHorizontalAlignment(JLabel.CENTER);
                        return renderer;
                    }
                },
                new ColumnInfo<ViewData, String>("方法名") {
                    @Nullable
                    @Override
                    public String valueOf(ViewData viewData) {
                        return viewData.dataItem.getQualifiedName();
                    }

                    @Override
                    public void setValue(ViewData viewData, String value) {
                        viewData.dataItem.setQualifiedName(value);
                    }

                    @Override
                    public @Nullable TableCellRenderer getRenderer(ViewData viewData) {
                        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                        renderer.setHorizontalAlignment(JLabel.CENTER);
                        return renderer;
                    }
                }
        };


    }

}
