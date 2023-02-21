package io.github.lgp547.anydoorplugin.util;

import com.intellij.openapi.diagnostic.Logger;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class VmUtil {

    private static final Logger log = Logger.getInstance(VmUtil.class);

    /**
     * -XX:+DisableAttachMechanism
     */
    public static void attachAsync(String pid, String jarFilePath, String param, BiConsumer<String, Exception> errHandle) {
        CompletableFuture.runAsync(() -> {
            String pidProcess = pid + " process";
            VirtualMachine vm = null;
            try {
                vm = VirtualMachine.attach(pid);
                vm.loadAgent(jarFilePath, param);
            } catch (IOException ioException) {
                if (ioException.getMessage() != null && ioException.getMessage().contains("Non-numeric value found")) {
                    log.warn("jdk lower version attach higher version, can ignore");
                } else {
                    errHandle.accept(pidProcess, ioException);
                }
            } catch (AgentLoadException agentLoadException) {
                if ("0".equals(agentLoadException.getMessage())) {
                    log.warn("jdk higher version attach lower version, can ignore");
                } else {
                    errHandle.accept(pidProcess, agentLoadException);
                }
            } catch (Exception e) {
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


}
