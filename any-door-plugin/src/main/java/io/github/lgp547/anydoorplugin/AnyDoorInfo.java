package io.github.lgp547.anydoorplugin;

import java.util.Map;
import java.util.function.UnaryOperator;

public interface AnyDoorInfo {
    String ANY_DOOR_NAME = "any-door";

    String ANY_DOOR_JAR_MIN_VERSION = "1.3.1";

    String ANY_DOOR_ALL_DEPENDENCE_JAR = "any-door-all-dependence.jar";

    UnaryOperator<String> ANY_DOOR_JAR_PATH = version -> "/io/github/lgp547/any-door/" + version + "/any-door-" + version + ".jar";


    /**
     * name -> path
     */
    Map<String, String> libMap = Map.of("any-door", "io.github.lgp547");
}