package io.github.lgp547.anydoorplugin.test;

import io.github.lgp547.anydoorplugin.util.JsonUtil;

public class JsonTest {
    public static void main(String[] args) {
        String s = JsonUtil.formatterJson("{\"ids\":[1,2,3,123]}");
        System.out.println(s);
    }
}
