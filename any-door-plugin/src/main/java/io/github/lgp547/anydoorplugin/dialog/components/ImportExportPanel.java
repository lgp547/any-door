package io.github.lgp547.anydoorplugin.dialog.components;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.*;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicRadioButtonUI;

import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.ui.JBDimension;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.utils.ParamUtil;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-20 10:54
 **/
public class ImportExportPanel extends JBPanel<ImportExportPanel> {

    public static final String IMPORT = "导入";
    public static final String EXPORT = "导出";

    private final String CURL = "cURL";
    private final String JSON = "JSON";


    private final String title;
    private final Project project;
    private final EditorTextField editorTextField;
    private final ButtonGroup buttonGroup;
    private final ParamDataItem dataItem;

    public ImportExportPanel(Project project, String title, ParamDataItem dataItem) {

        this.title = title;
        this.project = project;
        this.buttonGroup = new ButtonGroup();
        this.dataItem = dataItem;

        setPreferredSize(new JBDimension(670, 500));
        editorTextField = new EditorTextField();
        editorTextField.addSettingsProvider(editor -> {
            editor.setVerticalScrollbarVisible(true);
            editor.setHorizontalScrollbarVisible(true);
        });

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        if (Objects.equals(IMPORT, title)) {
            createImportPanel();
        } else {
            createExportPanel();
        }

    }

    private void createExportPanel() {
        editorTextField.setEnabled(false);
        createPanel();
    }

    private void createImportPanel() {
        createPanel();
    }

    private void createPanel() {
        initButtons();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 0;
        gbc.gridheight = -1;
        this.add(editorTextField, gbc);
    }

    private void initButtons() {
        ActionListener listener = e -> {
            MyRadioButton button = (MyRadioButton) e.getSource();
            if (button.isSelected() && Objects.equals(EXPORT, title)) {
                editorTextField.setText("");
                button.doExport(dataItem.getParam());
            }
        };

        MyRadioButton curl = addRadioButton(CURL);
        curl.setImportAction(() -> {
            String text = editorTextField.getText();
            return ParamUtil.importFromCurl(project, text);
        });


        MyRadioButton json = addRadioButton(JSON);
        json.setImportAction(editorTextField::getText);
        json.setExportAction(editorTextField::setText);

        curl.addActionListener(listener);
        json.addActionListener(listener);
        buttonGroup.add(json);
        buttonGroup.add(curl);
    }

    private MyRadioButton addRadioButton(String text) {
        MyRadioButton radioButton = new MyRadioButton(text);
        radioButton.setVerticalTextPosition(SwingConstants.CENTER);
        radioButton.setHorizontalTextPosition(SwingConstants.CENTER);
        radioButton.setPreferredSize(new Dimension(50, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE; // Do not resize the component
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;

        Color origin = radioButton.getBackground();
        radioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                radioButton.setBackground(origin.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                radioButton.setBackground(origin);
            }
        });

        this.add(radioButton, gbc);
        return radioButton;
    }

    public void copy() {
        String text = editorTextField.getText();
        StringSelection selection = new StringSelection(text);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    public void importData() {

        Enumeration<AbstractButton> elements = buttonGroup.getElements();
        while (elements.hasMoreElements()) {
            MyRadioButton button = (MyRadioButton) elements.nextElement();
            if (button.isSelected()) {
                String param = button.doImport();
                dataItem.setParam(param);
                break;
            }
        }
    }


    static class MyRadioButton extends JBRadioButton {
        private Supplier<String> importAction;
        private Consumer<String> exportAction;

        public MyRadioButton(String text) {
            super(text);
            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    JRadioButton button = (JRadioButton) e.getComponent();
                    Font currentFont = button.getFont();
                    // 根据组件的高度设置字体大小
                    float newSize = button.getHeight() * 0.75f;
                    button.setFont(currentFont.deriveFont(newSize));
                }
            });
        }

        @Override
        public void setUI(ButtonUI ui) {
            super.setUI(new BasicRadioButtonUI() {
                @Override
                public synchronized void paint(Graphics g, JComponent c) {
                    AbstractButton b = (AbstractButton) c;
                    ButtonModel model = b.getModel();

                    Font f = c.getFont();
                    g.setFont(f);

                    // Draw the text
                    FontMetrics fm = c.getFontMetrics(f);
                    int mnemonicIndex = b.getDisplayedMnemonicIndex();
                    if (model.isSelected()) {
                        g.setColor(b.getForeground().brighter()); // Change the color when selected

                        // Draw a thicker border at the bottom
                        int borderThickness = 2; // Change to the thickness you want

                        // Create a gradient from the center of the border to the edges
                        Color startColor = b.getForeground();
                        Color endColor = new Color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), 0); // Transparent
                        GradientPaint gradient = new GradientPaint(c.getWidth() / 2, 0, startColor,
                                c.getWidth(), 0, endColor, true);
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setPaint(gradient);
                        g2d.fillRect(0, c.getHeight() - borderThickness, c.getWidth(), borderThickness);
                    } else {
                        g.setColor(b.getForeground());
                    }

                    // Calculate the x position of the text
                    int textWidth = fm.stringWidth(b.getText());
                    int x = (b.getWidth() - textWidth) / 2;

                    BasicGraphicsUtils.drawStringUnderlineCharAt(g, b.getText(), mnemonicIndex,
                            x, fm.getAscent());

                }

                @Override
                public Dimension getPreferredSize(JComponent c) {
                    AbstractButton b = (AbstractButton) c;
                    String text = b.getText();
                    Icon icon = (b.isEnabled()) ? b.getIcon() : b.getDisabledIcon();

                    if ((icon == null) && (text == null)) {
                        return new Dimension(0, 0);
                    } else {
                        FontMetrics fm = b.getFontMetrics(b.getFont());
                        Rectangle iconR = new Rectangle();
                        Rectangle textR = new Rectangle();
                        Rectangle viewR = new Rectangle();
                        Insets i = c.getInsets();

                        viewR.x = i.left;
                        viewR.y = i.top;
                        viewR.width = b.getWidth() - (i.right + viewR.x);
                        viewR.height = b.getHeight() - (i.bottom + viewR.y);

                        SwingUtilities.layoutCompoundLabel(
                                c, fm, text, icon,
                                b.getVerticalAlignment(), b.getHorizontalAlignment(),
                                b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                                viewR, iconR, textR,
                                (text == null) ? 0 : b.getIconTextGap());

                        // Calculate the width and height
                        int width = Math.max(iconR.width, textR.width);
                        int height = Math.max(iconR.height, textR.height);

                        return new Dimension(width, height);
                    }
                }
            });
        }

        public void setImportAction(Supplier<String> importAction) {
            this.importAction = importAction;
        }

        public void setExportAction(Consumer<String> exportAction) {
            this.exportAction = exportAction;
        }

        public String doImport() {
            if (Objects.isNull(importAction)) {
                return null;
            }
            return importAction.get();
        }

        public void doExport(String text) {
            if (Objects.isNull(exportAction)) {
                return;
            }
            exportAction.accept(text);
        }

    }
}
