package io.github.lgp547.anydoorplugin.dialog;

import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBDimension;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public class ContentPanel extends JBPanel<ContentPanel> {

    private final JButton jButton1 = new JButton("gen json param");
    private final JButton jButton2 = new JButton("parse query param");
    private final JButton jButton3 = new JButton("gen query param");

    public ContentPanel(Component textArea) {
        super(new GridBagLayout());
        setPreferredSize(new JBDimension(600, 500));


        GridBagConstraints constraints1 = new GridBagConstraints();
        constraints1.anchor = GridBagConstraints.EAST;

        JPanel functionPanel = new JPanel();
        functionPanel.add(jButton1);
        functionPanel.add(jButton2);
        functionPanel.add(jButton3);
        add(functionPanel, constraints1);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 1;

        add(textArea, constraints);
    }


    public void addActionListener1(Consumer<ActionEvent> consumer) {
        jButton1.addActionListener(consumer::accept);
    }

    public void addActionListener2(Consumer<ActionEvent> consumer) {
        jButton2.addActionListener(consumer::accept);
    }

    public void addActionListener3(Consumer<ActionEvent> consumer) {
        jButton3.addActionListener(consumer::accept);
    }


}