package io.github.lgp547.anydoorplugin.dialog.components;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.*;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicArrowButton;

import com.intellij.ide.ui.laf.darcula.ui.DarculaComboBoxUI;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.event.Event;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;
import io.github.lgp547.anydoorplugin.dialog.event.Listener;
import io.github.lgp547.anydoorplugin.dialog.event.Multicaster;
import io.github.lgp547.anydoorplugin.dialog.event.impl.DisplayDataChangeEvent;
import io.github.lgp547.anydoorplugin.dialog.utils.EventHelper;
import io.github.lgp547.anydoorplugin.util.AnyDoorIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.ide.ui.laf.darcula.DarculaUIUtil.ARROW_BUTTON_WIDTH;
import static com.intellij.ide.ui.laf.darcula.DarculaUIUtil.COMPACT_HEIGHT;
import static com.intellij.ide.ui.laf.darcula.DarculaUIUtil.MINIMUM_HEIGHT;
import static com.intellij.ide.ui.laf.darcula.DarculaUIUtil.isCompact;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-18 11:12
 **/
public class MyComboBox extends JComboBox<ParamDataItem> implements Listener {

    private final Project project;
    private final Multicaster multicaster;

    public MyComboBox(Project project, Multicaster multicaster) {
        super(new MyComboBoxModel());
        this.project = project;
        this.multicaster = multicaster;

        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ParamDataItem) {
                    ParamDataItem item = (ParamDataItem) value;
                    setText(item.getName());
                }
                return this;
            }
        });

        addItemListener(new ItemListener() {

            ParamDataItem oldItem;

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    oldItem = (ParamDataItem) e.getItem();
                } else if (e.getStateChange() == ItemEvent.SELECTED) {

                    ParamDataItem item = (ParamDataItem) e.getItem();
                    Objects.requireNonNull(item);


                    if (Objects.equals(item, oldItem)) {
                        return;
                    }

                    multicaster.fireEvent(EventHelper.createSelectItemChangedEvent(item.getId()));
//                    int needDialog = 0;
//                    if (Objects.nonNull(oldItem.getId())) {
//                        if (context.paramChanged()) {
//                            needDialog = 1;
//                        }
//                    } else {
//                        if (context.paramChanged()) {
//                            needDialog = 2;
//                        } else {
//                            needDialog = 3;
//                        }
//                    }
//                    if (needDialog == 3) {
//                        context.removeNotNeedItem();
//                        model.removeElement(oldItem);
//                    }
//
//                    if (needDialog == 0 || needDialog == 3) {
//                        context.selectItem(item);
//                        return;
//                    }
//
//
//                    final int finalNeedDialog = needDialog;
//                    new DialogWrapper(project, true, DialogWrapper.IdeModalityType.IDE) {
//
//                        {
//                            init();
//                        }
//
//                        @Override
//                        protected @Nullable JComponent createCenterPanel() {
//                            JPanel panel = new JPanel(new BorderLayout());
//                            panel.setPreferredSize(new JBDimension(350, 30));
//                            String msg = finalNeedDialog == 1 ? "You have modified has not been saved and is still switch?" : "You have not saved the current record, do you want to switch?";
//                            JBLabel label = new JBLabel(msg);
//                            panel.add(label, BorderLayout.CENTER);
//                            return panel;
//                        }
//
//                        @Override
//                        protected void doOKAction() {
//                            super.doOKAction();
//
//                            if (finalNeedDialog == 1) {
//                                context.selectItem(item);
//                            } else {
//                                context.removeNotNeedItem();
//                                context.selectItem(item);
//                            }
//
//                        }
//
//                        @Override
//                        public void doCancelAction() {
//                            super.doCancelAction();
//                            model.setSelectedItem(oldItem);
//                        }
//                    }.show();
                }
            }
        });
    }

    @Override
    public void setUI(ComboBoxUI ui) {
        super.setUI(new DarculaComboBoxUI() {

            @Override
            protected JButton createArrowButton() {
                Color bg = comboBox.getBackground();
                Color fg = comboBox.getForeground();
                JButton button = new BasicArrowButton(SwingConstants.SOUTH, bg, fg, fg, fg) {

                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        Rectangle r = new Rectangle(getSize());
                        JBInsets.removeFrom(r, JBUI.insets(1, 0, 1, 1));

                        try {
                            int yOffset = (getHeight() - getIcon().getIconHeight()) / 2;
                            getIcon().paintIcon(this, g2, 0, yOffset);
                        } finally {
                            g2.dispose();
                        }
                    }

                    @Override
                    public Dimension getPreferredSize() {
                        return getArrowButtonPreferredSize(comboBox);
                    }
                };
                button.setIcon(AnyDoorIcons.edit_icon);
                button.setBorder(JBUI.Borders.empty());

                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        button.setIcon(AnyDoorIcons.edit_hover_icon);
                        button.setBackground(bg.brighter());
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        button.setIcon(AnyDoorIcons.edit_icon);
                        button.setBackground(bg);
                    }


                    @Override
                    public void mouseClicked(MouseEvent e) {
                        MyComboBoxModel model = (MyComboBoxModel) getModel();
                        ParamDataItem selectedItem = (ParamDataItem) model.getSelectedItem();

                        SaveDialog saveDialog = new SaveDialog(project, dialog -> {
                            String name = dialog.getName().trim();

                            selectedItem.setName(name);
                            multicaster.fireEvent(EventHelper.createUpdateItemNameEvent(selectedItem));
                        });
                        saveDialog.setName(selectedItem.getName());
                        saveDialog.show();
                    }
                });
                return button;
            }

            @NotNull
            Dimension getArrowButtonPreferredSize(@Nullable JComboBox comboBox) {
                Insets i = comboBox != null ? comboBox.getInsets() : getDefaultComboBoxInsets();
                int height = (isCompact(comboBox) ? COMPACT_HEIGHT.get() : MINIMUM_HEIGHT.get()) + i.top + i.bottom;
                return new Dimension(ARROW_BUTTON_WIDTH.get() + i.left, height);
            }

            private JBInsets getDefaultComboBoxInsets() {
                return JBUI.insets(3);
            }
        });
    }

    @Override
    public void onEvent(Event event) {
        if (Objects.equals(event.getType(), EventType.DISPLAY_DATA_CHANGE)) {
            DisplayDataChangeEvent changeEvent = (DisplayDataChangeEvent) event;
            MyComboBoxModel model = (MyComboBoxModel) getModel();
            model.refreshAll(changeEvent.getDisplayList(), changeEvent.getSelectedItem());
        }
    }


    static class MyComboBoxModel extends DefaultComboBoxModel<ParamDataItem> {

        public MyComboBoxModel() {
            super();
        }

        public void refreshAll(List<ParamDataItem> displayList, ParamDataItem selectedItem) {
            removeAllElements();
            addAll(displayList);
            setSelectedItem(selectedItem);
        }
    }
}
