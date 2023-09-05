package io.github.lgp547.anydoorplugin.dialog.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.*;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-08 10:23
 **/
public class CustomToolbar extends JToolBar {
    public CustomToolbar() {
        super();
    }

    public JButton addToolButton(String tip, Icon icon, Icon hoverIcon, Consumer<ActionEvent> consumer) {
        JButton button = new JButton();
        button.setToolTipText(tip);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addActionListener(consumer::accept);
        button.setIcon(icon);

        button.addMouseListener(new MouseAdapter() {
            private final Color background = button.getBackground();
            @Override
            public void mouseEntered(MouseEvent e) {
                // 鼠标进入按钮时，提高亮度
                button.setIcon(hoverIcon);
                button.setBackground(background.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // 鼠标离开按钮时，恢复原来的颜色
                button.setIcon(icon);
                button.setBackground(background);
            }
        });
        this.add(button);

        return button;
    }

    public JButton addToolButton(String text,  Consumer<ActionEvent> consumer) {
        JButton button = new JButton();
        button.setText(text);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addActionListener(consumer::accept);

        button.addMouseListener(new MouseAdapter() {
            private final Color background = button.getBackground();
            private final Color foreground = button.getForeground();

            @Override
            public void mouseEntered(MouseEvent e) {
                // 鼠标进入按钮时，提高亮度
                button.setBackground(background.brighter());
                button.setForeground(foreground.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // 鼠标离开按钮时，恢复原来的颜色
                button.setBackground(background);
                button.setForeground(foreground);
            }
        });
        this.add(button);

        return button;
    }
}
