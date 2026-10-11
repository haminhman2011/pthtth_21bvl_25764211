package Bai_5.Bai_10;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.concurrent.*;

/** UDP discovery va TCP service TIME/ECHO tren cung mot may. */
public class DiscoveryServer {
    public static void main(String[] args) {
        try {
            if (args.length == 1 && args[0].equals("--list")) { DiscoveryProtocol.list(); return; }
            if (args.length < 1 || args.length > 5) {
                System.err.println("Cách chạy: java Bai_5.Bai_10.DiscoveryServer <interface> [tcp_port=6010] [name=TimeEcho] [version=1.0] [udp_port=5010]");
                return;
            }
            NetworkInterface network = DiscoveryProtocol.network(args[0]);
            int tcpPort = args.length > 1 ? DiscoveryProtocol.port(args[1]) : 6010;
            String name = args.length > 2 ? args[2] : "TimeEcho";
            String version = args.length > 3 ? args[3] : "1.0";
            if (!DiscoveryProtocol.token(name) || !DiscoveryProtocol.token(version))
                throw new IllegalArgumentException("Tên/version: 1–64 ký tự chữ ASCII, số, _, -, .");
            int udpPort = args.length > 4 ? DiscoveryProtocol.port(args[4]) : DiscoveryProtocol.UDP_PORT;
            run(network, tcpPort, name, version, udpPort);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        }
    }

    private static void run(NetworkInterface network, int tcpPort, String name, String version, int udpPort) throws IOException {
        ExecutorService acceptor = Executors.newSingleThreadExecutor();
        ExecutorService workers = Executors.newFixedThreadPool(16);
        Semaphore slots = new Semaphore(16);
        Set<Socket> active = ConcurrentHashMap.newKeySet();
        MulticastSocket discovery = new MulticastSocket(null);
        ServerSocket tcp = new ServerSocket();
        InetSocketAddress group = new InetSocketAddress(InetAddress.getByName(DiscoveryProtocol.GROUP), udpPort);
        java.util.concurrent.atomic.AtomicBoolean joined = new java.util.concurrent.atomic.AtomicBoolean(false);
        Runnable cleanup = () -> {
            synchronized (discovery) {
                if (joined.compareAndSet(true, false)) {
                    try { discovery.leaveGroup(group, network); }
                    catch (IOException e) { System.err.println("Lỗi leaveGroup: " + e.getMessage()); }
                }
                discovery.close();
            }
            try { tcp.close(); } catch (IOException ignored) { }
            for (Socket socket : active) try { socket.close(); } catch (IOException ignored) { }
            acceptor.shutdownNow();
            workers.shutdownNow();
        };
        Thread hook = new Thread(cleanup, "discovery-cleanup");
        Runtime.getRuntime().addShutdownHook(hook);
        try {
            tcp.bind(new InetSocketAddress(tcpPort));
            synchronized (discovery) {
                discovery.setReuseAddress(true);
                discovery.bind(new InetSocketAddress(udpPort));
                discovery.setNetworkInterface(network);
                discovery.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
                discovery.joinGroup(group, network);
                joined.set(true);
            }
            acceptor.submit(() -> {
                try {
                    while (!tcp.isClosed()) {
                        Socket client = tcp.accept();
                        if (!slots.tryAcquire()) {
                            try (Socket rejected = client) {
                                sendLine(rejected, "ERR SERVER_BUSY");
                            }
                            continue;
                        }
                        active.add(client);
                        workers.execute(() -> {
                            try (Socket socket = client) { serve(socket, name, version); }
                            catch (IOException e) { System.err.println("Lỗi TCP client: " + e.getMessage()); }
                            finally { active.remove(client); slots.release(); }
                        });
                    }
                } catch (IOException e) {
                    if (!tcp.isClosed()) {
                        System.err.println("TCP listener lỗi, dừng discovery: " + e.getMessage());
                        discovery.close();
                    }
                }
            });
            System.out.println("READY SERVICE " + name + " " + tcpPort + " " + version
                    + "; UDP=" + udpPort + "; group=" + DiscoveryProtocol.GROUP + "; interface=" + network.getName());
            byte[] announcement = ("SERVICE " + name + " " + tcpPort + " " + version).getBytes(StandardCharsets.UTF_8);
            while (!discovery.isClosed()) {
                DatagramPacket request = new DatagramPacket(new byte[DiscoveryProtocol.MAX_PACKET + 1], DiscoveryProtocol.MAX_PACKET + 1);
                discovery.receive(request);
                String command = new String(request.getData(), request.getOffset(), request.getLength(), StandardCharsets.UTF_8);
                if (command.equals("DISCOVER_SERVICE")) {
                    // Unicast ve dia chi/cong nguon cua yeu cau discovery.
                    discovery.send(new DatagramPacket(announcement, announcement.length, request.getAddress(), request.getPort()));
                }
            }
        } finally {
            cleanup.run();
            try { Runtime.getRuntime().removeShutdownHook(hook); } catch (IllegalStateException ignored) { }
        }
    }

    private static void sendLine(Socket socket, String text) throws IOException {
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        output.write(text);
        output.newLine();
        output.flush();
    }

    private static void serve(Socket socket, String name, String version) throws IOException {
        socket.setSoTimeout(60_000);
        sendLine(socket, "WELCOME " + name + " " + version);
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        String command;
        while ((command = input.readLine()) != null) {
            if (command.equals("QUIT")) { sendLine(socket, "BYE"); return; }
            if (command.equals("TIME")) sendLine(socket, "OK " + OffsetDateTime.now());
            else if (command.startsWith("ECHO ")) sendLine(socket, "OK " + command.substring(5));
            else sendLine(socket, "ERR UNKNOWN_COMMAND");
        }
    }
}
