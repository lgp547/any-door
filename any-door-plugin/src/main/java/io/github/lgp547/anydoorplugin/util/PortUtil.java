package io.github.lgp547.anydoorplugin.util;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class PortUtil {
    public static boolean isPortAvailable(int i) {
        try (Socket socket = new Socket("127.0.0.1", i)) {
            return false;
        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

}
