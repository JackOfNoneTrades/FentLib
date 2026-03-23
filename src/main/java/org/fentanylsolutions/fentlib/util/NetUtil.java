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

    /**
     * Normalizes an HTTP(S) URL: trims, prepends {@code https://} if no scheme
     * is present, and strips trailing slashes.
     *
     * @return the normalized URL, or null for blank/null input
     */
    public static String normalizeHttpUrl(String raw) {
        String value = StringUtil.trimToNull(raw);
        if (value == null) return null;
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            value = "https://" + value;
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value.isEmpty() ? null : value;
    }
}
