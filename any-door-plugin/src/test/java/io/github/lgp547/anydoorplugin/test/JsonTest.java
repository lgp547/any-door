package io.github.lgp547.anydoorplugin.test;

import io.github.lgp547.anydoorplugin.util.JsonUtil;

public class JsonTest {
    /**
     * 默认的：
     * {
     *   "ids" : [ 1, 2, 3, 123 ]
     * }
     * 当前的：
     * {
     * 	"ids": [1, 2, 3, 123]
     * }
     */
    public static void main(String[] args) {
        String s = JsonUtil.formatterJson("{\"ids\":[1,2,3,123]}");
        System.out.println(s);
    }
}
