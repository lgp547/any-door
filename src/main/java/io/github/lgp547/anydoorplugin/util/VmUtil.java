package io.github.lgp547.anydoorplugin.util;

import com.sun.tools.attach.VirtualMachine;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class VmUtil {

    /**
     * -XX:+DisableAttachMechanism
     */
    public static void attachAsync(String pid, String jarFilePath, String param, BiConsumer<String, Exception> errHandle) {
        CompletableFuture.runAsync(() -> {
            try {
                VirtualMachine vm = VirtualMachine.attach(pid);
                vm.loadAgent(jarFilePath, param);
            } catch (Exception e) {
                errHandle.accept(pid, e);
            }
        });
    }


}
