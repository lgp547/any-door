package io.github.lgp547.anydoorplugin.data.domain;

import java.util.Vector;

import io.github.lgp547.anydoorplugin.dialog.ParamListTest;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-29 11:35
 **/
public class ParamDataItem extends DataItem {

    private String name;

    private String qualifiedName;

    private String param;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Vector<?> convert2Vector() {
        return new Vector<>() {{
            add(false);
            add(id);
            add(name);
            add(qualifiedName);
            add(param);
        }};
    }

    public ParamListTest.ViewData toViewData() {
        return new ParamListTest.ViewData(false, id, name, qualifiedName);
    }
}
