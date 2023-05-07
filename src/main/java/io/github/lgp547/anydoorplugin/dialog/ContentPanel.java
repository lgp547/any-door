package io.github.lgp547.anydoorplugin.dialog;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBDimension;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public class ContentPanel extends JBPanel<ContentPanel> {

    private final JButton cacheButton = new JButton("cache param");
    private final JButton simpleButton = new JButton("simple param");
    private final JButton jsonButton = new JButton("json param");
    private final JButton jsonToQueryButton = new JButton("json to query");
    private final JButton queryToJsonButton = new JButton("query to json");

    private final JBTextField runNum = new JBTextField( "1",10);
    private final JBCheckBox isConcurrent = new JBCheckBox("Concurrent run", true);

    public ContentPanel(Component textArea) {
        super(new GridBagLayout());
        setPreferredSize(new JBDimension(600, 500));


        GridBagConstraints constraints1 = new GridBagConstraints();
        constraints1.anchor = GridBagConstraints.EAST;

        JPanel functionPanel = new JPanel();
        functionPanel.add(cacheButton);
        functionPanel.add(simpleButton);
        functionPanel.add(jsonButton);
        functionPanel.add(jsonToQueryButton);
        functionPanel.add(queryToJsonButton);
        add(functionPanel, constraints1);

        GridBagConstraints constraints2 = new GridBagConstraints();
        constraints2.anchor = GridBagConstraints.EAST;
        constraints2.gridx = 0;
        constraints2.gridy = 1;
        JPanel functionPanel1 = new JPanel();
        functionPanel1.add(new JBLabel("Run num"));
        functionPanel1.add(runNum);
        functionPanel1.add(isConcurrent);
        add(functionPanel1, constraints2);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 2;

        add(textArea, constraints);
    }


    public void addCacheButtonListener(Consumer<ActionEvent> consumer) {
        cacheButton.addActionListener(consumer::accept);
    }

    public void addSimpleButtonListener(Consumer<ActionEvent> consumer) {
        simpleButton.addActionListener(consumer::accept);
    }

    public void addJsonButtonListener(Consumer<ActionEvent> consumer) {
        jsonButton.addActionListener(consumer::accept);
    }

    public void addJsonToQueryButtonListener(Consumer<ActionEvent> consumer) {
        jsonToQueryButton.addActionListener(consumer::accept);
    }

    public void addQueryToJsonButtonListener(Consumer<ActionEvent> consumer) {
        queryToJsonButton.addActionListener(consumer::accept);
    }


}