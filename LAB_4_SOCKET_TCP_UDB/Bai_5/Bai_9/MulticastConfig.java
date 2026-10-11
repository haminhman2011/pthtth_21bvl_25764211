package Bai_5.Bai_9;

import java.net.*;
import java.util.Enumeration;

final class MulticastConfig {
    static final String GROUP = "239.255.0.1";
    static final int DEFAULT_PORT = 5009;
    private MulticastConfig() { }

    static void listInterfaces() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface network = interfaces.nextElement();
            System.out.println("name=" + network.getName() + "; index=" + network.getIndex()
                    + "; display=" + network.getDisplayName() + "; up=" + network.isUp()
                    + "; multicast=" + network.supportsMulticast() + "; loopback=" + network.isLoopback());
            Enumeration<InetAddress> addresses = network.getInetAddresses();
            while (addresses.hasMoreElements()) System.out.println("  " + addresses.nextElement().getHostAddress());
        }
    }

    static NetworkInterface network(String value) throws SocketException {
        NetworkInterface network = NetworkInterface.getByName(value);
        if (network == null && value.matches("[0-9]+")) {
            network = NetworkInterface.getByIndex(Integer.parseInt(value));
        }
        if (network == null) throw new IllegalArgumentException("Không tìm thấy interface: " + value);
        if (!network.isUp()) throw new IllegalArgumentException("Interface chưa hoạt động: " + value);
        if (!network.supportsMulticast()) {
            System.err.println("Lưu ý: interface báo không hỗ trợ multicast; phép thử có thể thất bại trên hệ điều hành này.");
        }
        return network;
    }

    static int port(String value) {
        int port = Integer.parseInt(value);
        if (port < 1 || port > 65535) throw new IllegalArgumentException("Port phải từ 1 đến 65535");
        return port;
    }
}
