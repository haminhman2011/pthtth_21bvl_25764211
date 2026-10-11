package Bai_5.Bai_10;

import java.net.*;
import java.util.Enumeration;

final class DiscoveryProtocol {
    static final String GROUP = "239.255.0.1";
    static final int UDP_PORT = 5010;
    static final int MAX_PACKET = 1400;
    private DiscoveryProtocol() { }

    static int port(String text) {
        int port = Integer.parseInt(text);
        if (port < 1 || port > 65535) throw new IllegalArgumentException("Port phải từ 1 đến 65535");
        return port;
    }

    static NetworkInterface network(String text) throws SocketException {
        NetworkInterface network = NetworkInterface.getByName(text);
        if (network == null && text.matches("[0-9]+")) network = NetworkInterface.getByIndex(Integer.parseInt(text));
        if (network == null || !network.isUp()) throw new IllegalArgumentException("Interface không tồn tại hoặc chưa hoạt động: " + text);
        boolean ipv4 = false;
        Enumeration<InetAddress> addresses = network.getInetAddresses();
        while (addresses.hasMoreElements()) if (addresses.nextElement() instanceof Inet4Address) ipv4 = true;
        if (!ipv4) throw new IllegalArgumentException("Interface cần có địa chỉ IPv4");
        return network;
    }

    static void list() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface network = interfaces.nextElement();
            System.out.println("name=" + network.getName() + "; index=" + network.getIndex()
                    + "; up=" + network.isUp() + "; multicast=" + network.supportsMulticast()
                    + "; loopback=" + network.isLoopback() + "; display=" + network.getDisplayName());
            Enumeration<InetAddress> addresses = network.getInetAddresses();
            while (addresses.hasMoreElements()) System.out.println("  " + addresses.nextElement().getHostAddress());
        }
    }

    static boolean token(String value) { return value.matches("[A-Za-z0-9_.-]{1,64}"); }
}
