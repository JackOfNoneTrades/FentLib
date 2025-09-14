package org.fentanylsolutions.fentlib.util;

import java.net.SocketAddress;

public class NetUtil {

    public static String[] parseAddress(SocketAddress address) {
        return parseAddress(address.toString());
    }

    public static String[] parseAddress(String address) {
        if (address == null || address.isEmpty()) {
            return new String[] { null, null };
        }

        if (address.startsWith("/")) {
            address = address.substring(1);
        }

        int lastColon = address.lastIndexOf(':');

        if (lastColon == -1) {
            // No colon at all — assume it's just an IP with no port
            return new String[] { address, null };
        }

        // Check if there are multiple colons (probably IPv6) and no port
        boolean hasPort = true;
        String portPart = address.substring(lastColon + 1);
        for (char c : portPart.toCharArray()) {
            if (!Character.isDigit(c)) {
                hasPort = false;
                break;
            }
        }

        if (!hasPort) {
            return new String[] { address, null };
        }

        String ip = address.substring(0, lastColon);
        String port = portPart;
        return new String[] { ip, port };
    }
}
