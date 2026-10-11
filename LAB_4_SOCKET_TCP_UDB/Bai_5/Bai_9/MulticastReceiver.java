package Bai_5.Bai_9;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class MulticastReceiver {
    public static void main(String[] args) {
        try {
            if (args.length == 1 && args[0].equals("--list")) {
                MulticastConfig.listInterfaces();
                return;
            }
            if (args.length < 1 || args.length > 4) {
                System.err.println("Cách chạy: java Bai_5.Bai_9.MulticastReceiver <interface_name|index> [port] [count=0] [timeout_ms=10000]");
                System.err.println("count=0: nhận liên tục; nhấn Ctrl+C để dừng.");
                return;
            }
            NetworkInterface network = MulticastConfig.network(args[0]);
            int port = args.length > 1 ? MulticastConfig.port(args[1]) : MulticastConfig.DEFAULT_PORT;
            int count = args.length > 2 ? Integer.parseInt(args[2]) : 0;
            int timeout = args.length > 3 ? Integer.parseInt(args[3]) : 10_000;
            if (count < 0 || timeout < 1) throw new IllegalArgumentException("count >= 0, timeout_ms > 0");
            receive(network, port, count, timeout);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi receiver: " + e.getMessage());
        }
    }

    private static void receive(NetworkInterface network, int port, int count, int timeout) throws IOException {
        InetSocketAddress group = new InetSocketAddress(InetAddress.getByName(MulticastConfig.GROUP), port);
        MulticastSocket socket = new MulticastSocket(null);
        AtomicBoolean joined = new AtomicBoolean(false);
        Runnable cleanup = () -> {
            synchronized (socket) {
                if (joined.compareAndSet(true, false)) {
                    try {
                        socket.leaveGroup(group, network);
                        System.out.println("LEFT " + MulticastConfig.GROUP + "; interface=" + network.getName());
                    } catch (IOException e) {
                        System.err.println("Không rời được group: " + e.getMessage());
                    }
                }
                socket.close();
            }
        };
        Thread hook = new Thread(cleanup, "multicast-leave-group");
        Runtime.getRuntime().addShutdownHook(hook);
        try {
            synchronized (socket) {
                socket.setReuseAddress(true); // Truoc bind de nhieu receiver cung cong.
                socket.bind(new InetSocketAddress(port));
                socket.setSoTimeout(timeout);
                socket.setNetworkInterface(network);
                socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
                socket.joinGroup(group, network);
                joined.set(true);
            }
            System.out.println("JOINED " + MulticastConfig.GROUP + ":" + port + "; interface="
                    + network.getName() + "; index=" + network.getIndex() + "; timeout=" + timeout + " ms");
            int received = 0;
            while (count == 0 || received < count) {
                DatagramPacket packet = new DatagramPacket(new byte[65507], 65507);
                try {
                    socket.receive(packet);
                    String message = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                    System.out.println("RECEIVED " + (++received) + " from " + packet.getSocketAddress() + ": " + message);
                } catch (SocketTimeoutException e) {
                    System.out.println("TIMEOUT: Không nhận được trong " + timeout + " ms; kiểm tra interface, Wi-Fi, VPN hoặc firewall.");
                    if (count > 0) break;
                }
            }
            System.out.println("TOTAL " + received);
        } finally {
            cleanup.run();
            try { Runtime.getRuntime().removeShutdownHook(hook); }
            catch (IllegalStateException ignored) { /* JVM dang shutdown, hook tu don dep. */ }
        }
    }
}
