package io.github.lgp547.anydoorplugin.dialog;

import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBDimension;

import javax.swing.*;
import java.awt.*;

public class ContentPanel extends JBPanel<ContentPanel> {

    public ContentPanel(JSONEditor textArea) {
        super(new GridBagLayout());
        setPreferredSize(new JBDimension(600, 500));


        GridBagConstraints constraints1 = new GridBagConstraints();
        constraints1.anchor = GridBagConstraints.EAST;

        JPanel functionPanel = new JPanel();
        JButton jButton1 = new JButton("gen json param");
        JButton jButton2 = new JButton("parse query param");
        JButton jButton3 = new JButton("gen query param");
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


        jButton1.addActionListener(e -> textArea.resetDefaultContent());
        jButton2.addActionListener(e -> textArea.parseQueryParam(textArea.getText()));
        jButton3.addActionListener(e -> textArea.genQueryParam(textArea.getText()));

        textArea.requestFocusInWindow();
        add(textArea, constraints);
    }










}