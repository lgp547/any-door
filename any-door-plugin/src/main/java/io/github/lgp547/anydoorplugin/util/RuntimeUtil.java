package io.github.lgp547.anydoorplugin.util;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

public class RuntimeUtil {

    public static String getProjectPid(String basePath, String projectName) {
        String exec = exec(basePath + "jps");
        Optional<String> first = Arrays.stream(exec.split("\n")).map(String::toUpperCase).filter(item -> item.contains(projectName.toUpperCase())).findFirst();
        return first.map(s -> StringUtils.substringBefore(s, " ")).orElse("");
    }

    public static String exec(String cmd) {
        if (StringUtils.isEmpty(cmd)) {
            throw new NullPointerException("Command is empty !");
        }
        Process process = null;
        InputStream inputStream = null;
        try {
            process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            inputStream = process.getInputStream();
            byte[] bytes = inputStream.readAllBytes();
            return new String(bytes, Charset.defaultCharset());
        } catch (IOException ignored) {
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (null != process) {
                process.destroy();
            }
        }
        return "";
    }


}
