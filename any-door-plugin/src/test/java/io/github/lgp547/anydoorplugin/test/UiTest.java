package io.github.lgp547.anydoorplugin.test;

import java.io.File;
import java.io.IOException;

import javax.swing.*;

import com.intellij.openapi.util.io.FileUtil;

public class UiTest {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(UiTest::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        ContentPanel contentPanel = new ContentPanel(new TextArea("lgp"));
//        frame.setContentPane(contentPanel);


//        // 显示窗口
//        frame.pack();
//        frame.setVisible(true);

    }


}
