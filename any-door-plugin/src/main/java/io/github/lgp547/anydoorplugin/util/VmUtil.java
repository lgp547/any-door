package io.github.lgp547.anydoorplugin.util;

import com.google.common.base.Charsets;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class VmUtil {

    private static final Logger log = Logger.getInstance(VmUtil.class);

    /**
     * -XX:+DisableAttachMechanism
     */
    public static void attachAsync(String pid, String jarFilePath, String param, String paramPath, BiConsumer<String, Exception> errHandle) {
        CompletableFuture.runAsync(() -> {
            // Exceed attach limit, go file transfer. file://paramPath
            String agentParam = param;
            if (agentParam.length() > 600 && flushFile(paramPath, agentParam)) {
                agentParam = "file://" + URLEncoder.encode(paramPath, Charsets.UTF_8);
            }

            String pidProcess = pid + " process";
            VirtualMachine vm = null;
            try {
                vm = VirtualMachine.attach(pid);
                vm.loadAgent(jarFilePath, agentParam);
            } catch (IOException ioException) {
                if (ioException.getMessage() != null && ioException.getMessage().contains("Non-numeric value found")) {
                    log.warn("jdk lower version attach higher version, can ignore");
                } else {
                    log.error("attachAsync error [pid:{} jarFilePath:{} param:{} errMsg:{}]", pid, jarFilePath, agentParam, ioException.getMessage());
                    errHandle.accept(pidProcess, ioException);
                }
            } catch (AgentLoadException agentLoadException) {
                if ("0".equals(agentLoadException.getMessage())) {
                    log.warn("jdk higher version attach lower version, can ignore");
                } else {
                    log.error("attachAsync error [pid:{} jarFilePath:{} param:{} errMsg:{}]", pid, jarFilePath, agentParam, agentLoadException.getMessage());
                    errHandle.accept(pidProcess, agentLoadException);
                }
            } catch (Exception e) {
                log.error("attachAsync error [pid:{} jarFilePath:{} param:{} errMsg:{}]", pid, jarFilePath, agentParam, e.getMessage());
                errHandle.accept(pidProcess, e);
            } finally {
                if (null != vm) {
                    try {
                        vm.detach();
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    /**
     * rewrite content to file
     */
    public static boolean flushFile(String filePath, String content) {
        File file = new File(filePath);
        try {
            FileUtil.writeToFile(file, content);
            return true;
        } catch (IOException e) {
            log.error("flushFile error [filePath:{} content:{} errMsg:{}]", filePath, content, e.getMessage());
        }
        return false;
    }

}
