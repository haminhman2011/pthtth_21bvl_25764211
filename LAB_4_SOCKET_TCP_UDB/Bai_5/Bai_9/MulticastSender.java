package Bai_5.Bai_9;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class MulticastSender {
    public static void main(String[] args) {
        try {
            if (args.length == 1 && args[0].equals("--list")) {
                MulticastConfig.listInterfaces();
                return;
            }
            if (args.length < 1 || args.length > 2) {
                System.err.println("Cách chạy: java Bai_5.Bai_9.MulticastSender <interface_name|index> [port]");
                System.err.println("Liệt kê interface: java Bai_5.Bai_9.MulticastSender --list");
                return;
            }
            NetworkInterface network = MulticastConfig.network(args[0]);
            int port = args.length == 2 ? MulticastConfig.port(args[1]) : MulticastConfig.DEFAULT_PORT;
            InetAddress group = InetAddress.getByName(MulticastConfig.GROUP);
            try (MulticastSocket socket = new MulticastSocket()) {
                socket.setNetworkInterface(network);
                socket.setTimeToLive(1);
                socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
                BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                System.out.println("Sender -> " + group.getHostAddress() + ":" + port
                        + "; interface=" + network.getName() + "; TTL=1; multicast loopback=true");
                System.out.println("Nhập thông báo; QUIT để thoát sender (không gửi QUIT).");
                String message;
                while ((message = console.readLine()) != null && !message.equals("QUIT")) {
                    byte[] data = message.getBytes(StandardCharsets.UTF_8);
                    if (data.length > 1400) {
                        System.err.println("Thông báo vượt 1400 byte UTF-8; không gửi.");
                        continue;
                    }
                    socket.send(new DatagramPacket(data, data.length, group, port));
                    System.out.println("SENT " + data.length + " byte: " + message);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi sender: " + e.getMessage());
        }
    }
}
