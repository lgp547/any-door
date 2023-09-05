package io.github.lgp547.anydoorplugin.data.domain;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-22 11:29
 **/
public class ParamIndexData extends DataItem {

    private String name;

    private String qualifiedMethodName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQualifiedMethodName() {
        return qualifiedMethodName;
    }

    public void setQualifiedMethodName(String qualifiedMethodName) {
        this.qualifiedMethodName = qualifiedMethodName;
    }
}
