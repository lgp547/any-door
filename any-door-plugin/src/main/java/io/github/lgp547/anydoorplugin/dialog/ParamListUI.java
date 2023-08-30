package io.github.lgp547.anydoorplugin.dialog;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.TableView;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import io.github.lgp547.anydoorplugin.action.AnyDoorAgainOpenAction;
import io.github.lgp547.anydoorplugin.action.AnyDoorPerformed;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.data.domain.ParamIndexData;
import io.github.lgp547.anydoorplugin.dialog.components.CustomToolbar;
import io.github.lgp547.anydoorplugin.dialog.event.Event;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;
import io.github.lgp547.anydoorplugin.dialog.event.DefaultMulticaster;
import io.github.lgp547.anydoorplugin.dialog.event.Listener;
import io.github.lgp547.anydoorplugin.dialog.utils.EventHelper;
import io.github.lgp547.anydoorplugin.dialog.utils.IdeClassUtil;
import io.github.lgp547.anydoorplugin.settings.AnyDoorSettingsState;
import io.github.lgp547.anydoorplugin.util.AnyDoorActionUtil;
import io.github.lgp547.anydoorplugin.util.AnyDoorIcons;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-16 20:35
 **/
public class ParamListUI extends JPanel implements Listener {

    private final JBScrollPane contentPanel;
    private final JToolBar toolBar;

    private final ParamListTableModel tableModel;
    private final TableView<ViewData> table;

    private final Project project;

    private JBPopup myPopup;
    private ClassDataContext context;

    public ParamListUI(Project project) {
        this.project = project;
        DefaultMulticaster.getInstance(project).addListener(this);


        toolBar = initToolBar();
        tableModel = new ParamListTableModel();
        table = new ParamListTable(tableModel);
        contentPanel = new JBScrollPane(table);

        //布局
        paramListLayout();

        registerListener();

        DumbService.getInstance(project).runWhenSmart(this::initLoadData);
    }


    private JToolBar initToolBar() {
        ParamListToolBar toolBar = new ParamListToolBar();
        toolBar.addToolButton("Delete", AnyDoorIcons.delete_icon, AnyDoorIcons.delete_hover_icon, e -> deleteAction());

        toolBar.addToolButton("Refresh", AnyDoorIcons.refresh_icon, AnyDoorIcons.refresh_hover_icon, e -> {
            context = DataContext.instance(project).getClassDataContextNoCache(context.clazz.getQualifiedName());

            tableModel.refreshAll(ViewData.toViewData(context.data));
        });

        toolBar.addToolButton("Find", AnyDoorIcons.search_icon, AnyDoorIcons.search_icon, e -> findAction());

        return toolBar;
    }

    private void findAction() {
        SearchTextField searchTextField = new SearchTextField();
        ListTableModel<ViewData> model = new ListTableModel<>(ViewData.SEARCH_COLUMN_NAMES);
        TableView<ViewData> searchTable = new TableView<>(model);
        searchTable.setShowGrid(false);
        searchTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = searchTable.rowAtPoint(e.getPoint());
                int col = searchTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0) {
                    ViewData item = model.getItem(row);
                    PsiMethod method = IdeClassUtil.findMethod(project, item.dataItem.getQualifiedName());
                    if (Objects.nonNull(method)) {
                        myPopup.cancel();
                        PsiNavigateUtil.navigate(method);
                    }
                }
            }
        });

        searchTextField.addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String text = searchTextField.getText();

                List<ParamIndexData> searchResults = DataContext.instance(project).search(text);

                model.setItems(ViewData.toViewData(searchResults));
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(500, 300));
        panel.add(searchTextField, BorderLayout.NORTH);
        panel.add(new JBScrollPane(searchTable), BorderLayout.CENTER);


        myPopup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(panel, searchTextField)
                .setFocusable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(true)
                .setCancelOnWindowDeactivation(true)
                .createPopup();

        Disposer.register(myPopup, () -> myPopup = null);

        myPopup.showInFocusCenter();
    }

    private void deleteAction() {
        List<ParamDataItem> dataItems = tableModel.getItems()
                .stream()
                .filter(v -> !v.isSelected())
                .map(ViewData::getDataItem)
                .collect(Collectors.toList());
        if (Objects.equals(dataItems.size(), tableModel.getItems().size())) {
            return;
        }


        new DialogWrapper(project, true, DialogWrapper.IdeModalityType.IDE) {
            {
                init();
            }

            @Override
            protected @Nullable JComponent createCenterPanel() {
                return new JLabel("Are you sure to delete the selected data？");
            }

            @Override
            protected void doOKAction() {
                List<ParamDataItem> removeItems = tableModel.getItems().stream().filter(ViewData::isSelected).map(ViewData::getDataItem).collect(Collectors.toList());
                if (removeItems.size() == 0) {
                    return;
                }
                DefaultMulticaster.getInstance(project).fireEvent(EventHelper.createGlobalRemoveDataChangeEvent(context.clazz.getQualifiedName(), removeItems.get(0).getQualifiedName(), removeItems));
                super.doOKAction();
            }
        }.show();
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
                    Long dataItemId = value.dataItem.getId();

                    int index = qName.lastIndexOf("#");
                    String className = qName.substring(0, index);

                    AnyDoorSettingsState service = project.getService(AnyDoorSettingsState.class);
                    PsiClass psiClass = IdeClassUtil.findClass(project, className);
                    PsiMethod psiMethod = IdeClassUtil.findMethod(project, qName);
                    if (Objects.isNull(psiClass) || Objects.isNull(psiMethod)) {
                        NotifierUtil.notifyError(project, "class or method not found");
                        return;
                    }
                    new AnyDoorPerformed().doUseNewUI(service, project, psiClass, psiMethod, AnyDoorActionUtil.genCacheKey(psiClass, psiMethod),
                            () -> AnyDoorAgainOpenAction.PRE_METHOD_MAP.put(project.getName(), psiMethod), dataItemId);
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
                doReadAndRefresh(qualifiedName);
            }
        }
    }

    private void doReadAndRefresh(String qualifiedName) {
//        data = dataService.find(qualifiedName);
        context = DataContext.instance(project).getClassDataContext(qualifiedName);

        tableModel.refreshAll(ViewData.toViewData(context.data));
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

    @Override
    public void onEvent(Event event) {
        if (Objects.equals(event.getType(), EventType.DATA_SYNC)) {
            doReadAndRefresh(context.clazz.getQualifiedName());
        }
    }


    static class ParamListToolBar extends CustomToolbar {

        public ParamListToolBar() {
            super();
            this.setFloatable(false);

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

        public ViewData(ParamIndexData item) {
            ParamDataItem dataItem = new ParamDataItem();
            dataItem.setId(item.getId());
            dataItem.setName(item.getName());
            dataItem.setQualifiedName(item.getQualifiedMethodName());
            this.dataItem = dataItem;
        }

        public ParamDataItem getDataItem() {
            return dataItem;
        }

        public boolean isSelected() {
            return selected;
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

        public static List<ViewData> toViewData(List<ParamIndexData> data) {
            if (Objects.isNull(data)) {
                return Collections.emptyList();
            }
            return data
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

        static final ColumnInfo<ViewData, ?>[] SEARCH_COLUMN_NAMES = new ColumnInfo[]{
                new ColumnInfo<ViewData, String>("") {
                    @Nullable
                    @Override
                    public String valueOf(ViewData viewData) {
                        return viewData.dataItem.getName();
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
                new ColumnInfo<ViewData, String>("") {
                    @Nullable
                    @Override
                    public String valueOf(ViewData viewData) {
                        String qualifiedName = viewData.dataItem.getQualifiedName();
                        if (Objects.nonNull(qualifiedName) && qualifiedName.contains("#")) {
                            int index = qualifiedName.lastIndexOf("#");
                            return qualifiedName.substring(index + 1);
                        }
                        return qualifiedName;
                    }


                    @Override
                    public @Nullable TableCellRenderer getRenderer(ViewData viewData) {
                        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                        renderer.setHorizontalAlignment(JLabel.CENTER);
                        return renderer;
                    }
                },
                new ColumnInfo<ViewData, String>("") {
                    @Nullable
                    @Override
                    public String valueOf(ViewData viewData) {
                        String qualifiedName = viewData.dataItem.getQualifiedName();
                        if (Objects.nonNull(qualifiedName) && qualifiedName.contains("#")) {
                            int index = qualifiedName.lastIndexOf("#");
                            return qualifiedName.substring(0, index);
                        }
                        return qualifiedName;
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
                }
        };

    }

}
