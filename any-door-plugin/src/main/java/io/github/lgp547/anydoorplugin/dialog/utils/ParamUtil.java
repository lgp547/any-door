package io.github.lgp547.anydoorplugin.dialog.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-18 20:15
 **/
public class ParamUtil {

    public static String importFromCurl(Project project, String cUrl) {
        if (Objects.isNull(cUrl)) {
            NotifierUtil.notifyError(project, "cURL is null");
            return "";
        }

        String[] args = cUrl.split(" ");

        Options options = new Options();
        options.addOption("d", true, "data");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            NotifierUtil.notifyError(project, "cURL parse error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        Map<String, String> params = new HashMap<>();
        if (cmd.hasOption("d")) {
            String[] dataParams = cmd.getOptionValues("d");
            for (String param : dataParams) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }

        Gson gson = new Gson();
        return gson.toJson(params);
    }
}
