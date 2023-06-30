package io.github.lgp547.anydoorplugin.test;

import io.github.lgp547.anydoorplugin.dialog.ContentPanel;
import io.github.lgp547.anydoorplugin.dialog.ParamListUI;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class UiTest {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(UiTest::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        ContentPanel contentPanel = new ContentPanel(new TextArea("lgp"));
//        frame.setContentPane(contentPanel);
        ParamListUI ui = new ParamListUI();

        ui.setPreferredSize(new Dimension(500, 800));
        ui.setBackground(Color.BLACK);
        Vector<Vector<Object>> vectors = new Vector<>();
        Vector<Object> v1 = new Vector<>();
        v1.add(true);
        v1.add(1);
        v1.add("lgp");
        v1.add("2021-06-17");
        Vector<Object> v2 = new Vector<>();
        v2.add(false);
        v2.add(2);
        v2.add("zz");
        v2.add("2021-06-17");
        vectors.add(v1);
        vectors.add(v2);
        ui.initDataList(vectors);

        frame.setContentPane(ui);

        // 显示窗口
        frame.pack();
        frame.setVisible(true);
    }


}
