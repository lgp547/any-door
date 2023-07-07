package io.github.lgp547.anydoorplugin.dialog;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.intellij.ide.structureView.newStructureView.StructureViewComponent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorWithProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.messages.MessageBusConnection;
import io.github.lgp547.anydoorplugin.action.AnyDoorPerformed;
import io.github.lgp547.anydoorplugin.data.DataService;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.data.impl.ParamDataService;
import io.github.lgp547.anydoorplugin.dto.ParamCacheDto;
import io.github.lgp547.anydoorplugin.settings.AnyDoorSettingsState;
import io.github.lgp547.anydoorplugin.util.AnyDoorIcons;
import io.github.lgp547.anydoorplugin.util.JsonUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-16 20:35
 **/
public class ParamListUI extends JPanel {
    private final Project project;

    private final JToolBar toolBar;
    private final JBScrollPane contentPanel;

    private final DataService<ParamDataItem> dataService;
    private JBTable contentTable;

    public ParamListUI(Project project) {
        this.project = project;
        dataService = project.getService(ParamDataService.class);

        toolBar = initToolBar();
        contentPanel = initPanel();

        //布局
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
//        contentPanel.setBackground(Color.GREEN);
        this.add(contentPanel, gbc);

//        registerFileEditorToRefreshDataList();
        initDataList();
    }

    private void registerFileEditorToRefreshDataList(){
        MessageBusConnection connection = project.getMessageBus().connect();

        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                VirtualFile newFile = event.getNewFile();
                if (newFile != null) {
                    String qualifiedName = getQualifiedName(newFile);
                    if (qualifiedName != null) {
                        Data<ParamDataItem> data = dataService.read(qualifiedName);
                        refreshTableData(data);
                    }
                }
            }
        });
    }

    private void refreshTableData(Data<ParamDataItem> data){
        ParamListTableModel model = (ParamListTableModel) contentTable.getModel();
        ParamListTableModel tableModel = (ParamListTableModel) contentTable.getModel();
        tableModel.getDataVector().clear();
        tableModel.fillTableData(data);
        model.fireTableDataChanged();
    }

    private JBScrollPane initPanel() {
        contentTable = initTable();
        return new JBScrollPane(contentTable);
    }

    public void initDataList() {
//        VirtualFile[] files = FileEditorManager.getInstance(project).getSelectedFiles();
//        if (files.length > 0) {
//            VirtualFile file = files[0];
//            String qualifiedName = getQualifiedName(file);
//            if (Objects.nonNull(qualifiedName)) {
//                // 读取数据
//                Data<ParamDataItem> data = dataService.read(qualifiedName);
//                refreshTableData(data);
//            }
//        }
        Vector<Vector<Object>> dataVector = new Vector<>();
        Vector<Object> rowVector = new Vector<>();
        rowVector.add(true);
        rowVector.add("1");
        rowVector.add("aaaa");
        rowVector.add("com.test.bpm.ProcessApp#startProcess");
        rowVector.add("{}");
        dataVector.add(rowVector);
        ParamListTableModel model = (ParamListTableModel) contentTable.getModel();
        dataVector.forEach(model::addRow);
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

//    private void addComponent(Component component, GridBagConstraints gbc, int gridx, int gridy, int gridwidth, int gridheight, int weightx, int weighty) {
//        gbc.gridx = gridx;
//        gbc.gridy = gridy;
//        gbc.gridwidth = gridwidth;
//        gbc.gridheight = gridheight;
//        gbc.weightx = weightx;
//        gbc.weighty = weighty;
//        this.add(component, gbc);
//    }

    private ParamListToolBar initToolBar() {
        ParamListToolBar toolBar = new ParamListToolBar();
        toolBar.addToolButton("新增", AnyDoorIcons.add_icon, AnyDoorIcons.add_hover_icon, e -> {
            System.out.println("新增");
        });
        toolBar.addToolButton("删除", AnyDoorIcons.delete_icon, AnyDoorIcons.delete_hover_icon, e -> {
            System.out.println("删除");
        });
        toolBar.addToolButton("刷新", AnyDoorIcons.refresh_icon, AnyDoorIcons.refresh_hover_icon, e -> {
            System.out.println("刷新");
        });
        toolBar.addToolButton("查找", AnyDoorIcons.search_icon, AnyDoorIcons.search_icon, e -> {
            System.out.println("查找");
        });

        return toolBar;
    }

    private JBTable initTable() {
        JBTable table = new JBTable();

        ParamListTableModel tableModel = new ParamListTableModel();
        tableModel.setColumnIdentifiers(new String[]{"", "ID", "名称", "方法名", "参数"});
        table.setModel(tableModel);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, renderer);

        table.getTableHeader().setDefaultRenderer(renderer);

        TableColumn tableColumn = table.getColumnModel().getColumn(0);
        tableColumn.setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setContentAreaFilled(false);
                checkBox.setOpaque(false);
                checkBox.setBorderPainted(false);
                checkBox.setFocusPainted(false);
                checkBox.setSelected((Boolean) value);
                checkBox.setHorizontalAlignment(JLabel.CENTER);
                return checkBox;
            }

        });
        tableColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()));

        tableColumnWidth(table);
//        fitTableColumns(table);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {     // 检查双击事件
                    int row = table.getSelectedRow(); // 获取选中行的索引
                    System.out.println("Row " + row + " data: ");
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        System.out.print(" " + table.getValueAt(row, i));
                    }
                    System.out.println();
                    String qName = (String) table.getValueAt(row, 3);
                    String param = (String) table.getValueAt(row, 4);
                    int index = qName.lastIndexOf("#");
                    String className = qName.substring(0, index);
                    String methodName = qName.substring(index + 1);
                    System.out.println(className + " " + methodName + " " + param);
                    PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
                    PsiMethod method = psiClass.findMethodsByName(methodName, false)[0];
                    System.out.println(psiClass);
                    System.out.println(method);
//                    Optional<AnyDoorSettingsState> anyDoorSettingsStateOpt = AnyDoorSettingsState.getAnyDoorSettingsStateNotExc(project);
//                    if (anyDoorSettingsStateOpt.isEmpty()) {
//                        return;
//                    }
//
//                    AnyDoorSettingsState service = anyDoorSettingsStateOpt.get();
//                    TextAreaDialog dialog = new TextAreaDialog(project, String.format("fill method(%s) param", methodName), method.getParameterList(), new ParamCacheDto(param), service);
                    new AnyDoorPerformed().invoke(project, method, () -> {});
                }
            }
        });
        return table;
    }

    private void tableColumnWidth(JTable table) {
        int columnCount = table.getColumnModel().getColumnCount();
        TableColumn firstColumn = table.getColumnModel().getColumn(0);
        firstColumn.setPreferredWidth(30);
        firstColumn.setMaxWidth(30);
        firstColumn.setMinWidth(30);

        TableColumn secondColumn = table.getColumnModel().getColumn(1);
        secondColumn.setPreferredWidth(30);
        secondColumn.setMaxWidth(30);
        secondColumn.setMinWidth(30);

        TableColumn lastColumn = table.getColumnModel().getColumn(columnCount - 1);
        lastColumn.setPreferredWidth(100);
        lastColumn.setMaxWidth(100);
        lastColumn.setMinWidth(100);
    }

    public void fitTableColumns(JTable table) {               //設置table的列寬隨內容調整

        JTableHeader header = table.getTableHeader();

        int rowCount = table.getRowCount();

        Enumeration columns = table.getColumnModel().getColumns();

        while (columns.hasMoreElements()) {
            TableColumn column = (TableColumn) columns.nextElement();

            int col = header.getColumnModel().getColumnIndex(

                    column.getIdentifier());

            int width = (int) table.getTableHeader().getDefaultRenderer()

                    .getTableCellRendererComponent(table,

                            column.getIdentifier(), false, false, -1, col)

                    .getPreferredSize().getWidth();

            for (int row = 0; row < rowCount; row++) {
                int preferredWidth = (int) table.getCellRenderer(row, col)

                        .getTableCellRendererComponent(table,

                                table.getValueAt(row, col), false, false,

                                row, col).getPreferredSize().getWidth();

                width = Math.max(width, preferredWidth);

            }

            header.setResizingColumn(column);

            column.setWidth(width + table.getIntercellSpacing().width);
        }

    }

    static class ParamListToolBar extends JToolBar {


        public ParamListToolBar() {
            super();
            this.setFloatable(false);
        }

        public void addToolButton(String tip, Icon icon, Icon hoverIcon, Consumer<ActionEvent> consumer) {
            JButton button = new JButton();
            button.setToolTipText(tip);
//            button.setBackground(Color.MAGENTA);
            button.setContentAreaFilled(false);
            button.setOpaque(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.addActionListener(consumer::accept);
            button.setIcon(icon);

            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    // 鼠标进入按钮时，提高亮度
                    button.setIcon(hoverIcon);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    // 鼠标离开按钮时，恢复原来的颜色
                    button.setIcon(icon);
                }
            });
            this.add(button);
        }
    }

    static class ParamListTableModel extends DefaultTableModel {

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        public void fillTableData(Data<ParamDataItem> data) {
            data.getDataList()
                    .stream()
                    .map(ParamDataItem::convert2Vector)
                    .forEach(this::addRow);
        }
    }
}
