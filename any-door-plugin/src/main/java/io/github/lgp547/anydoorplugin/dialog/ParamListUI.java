package io.github.lgp547.anydoorplugin.dialog;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import io.github.lgp547.anydoorplugin.util.AnyDoorIcons;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-16 20:35
 **/
public class ParamListUI extends JPanel {
    private final JToolBar toolBar;
    private final JBScrollPane contentPanel;
    private JBTable contentTable;

    public ParamListUI() {

        toolBar = initToolBar();
        contentPanel = initPanel();

        //布局
        GridBagLayout layout = new GridBagLayout();
        this.setLayout(layout);

        Point point = layout.getLayoutOrigin();

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
    }

    private JBScrollPane initPanel() {
        contentTable = initTable();
        return new JBScrollPane(contentTable);
    }

    public void initDataList(Vector<Vector<Object>> dataVector) {
        ParamListTableModel tableModel = (ParamListTableModel) contentTable.getModel();

        if (Objects.nonNull(dataVector)) {
            dataVector.forEach(tableModel::addRow);
        }
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
        tableModel.setColumnIdentifiers(new String[]{"", "ID", "名称", "最后修改时间"});
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
    }
}
