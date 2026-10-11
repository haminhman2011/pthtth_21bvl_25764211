package Bai_5.Bai_10;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DiscoveryClient {
    private static final class Service {
        final InetAddress address;
        final int port;
        final String name, version;
        Service(InetAddress address, int port, String name, String version) {
            this.address = address; this.port = port; this.name = name; this.version = version;
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length == 1 && args[0].equals("--list")) { DiscoveryProtocol.list(); return; }
            if (args.length < 1 || args.length > 3) {
                System.err.println("Cách chạy: java Bai_5.Bai_10.DiscoveryClient <interface> [timeout_ms=2000] [udp_port=5010]");
                return;
            }
            NetworkInterface network = DiscoveryProtocol.network(args[0]);
            int timeout = args.length > 1 ? Integer.parseInt(args[1]) : 2000;
            if (timeout < 1 || timeout > 60_000) throw new IllegalArgumentException("Timeout từ 1 đến 60000 ms");
            int udpPort = args.length > 2 ? DiscoveryProtocol.port(args[2]) : DiscoveryProtocol.UDP_PORT;
            List<Service> services = discover(network, timeout, udpPort);
            System.out.println("FOUND " + services.size() + " service(s)");
            for (int i = 0; i < services.size(); i++) {
                Service service = services.get(i);
                System.out.println((i + 1) + ". " + service.name + " " + service.address.getHostAddress()
                        + ":" + service.port + " version=" + service.version);
            }
            if (services.isEmpty()) {
                System.out.println("Không có phản hồi trong timeout; kiểm tra interface, server, VPN hoặc firewall.");
                return;
            }
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            System.out.println("Chọn số server để kết nối TCP, hoặc QUIT để thoát:");
            String choice;
            while ((choice = console.readLine()) != null && !choice.equals("QUIT")) {
                int index;
                try { index = Integer.parseInt(choice) - 1; }
                catch (NumberFormatException e) { System.out.println("Nhập số server hợp lệ:"); continue; }
                if (index < 0 || index >= services.size()) { System.out.println("Nhập số server hợp lệ:"); continue; }
                connect(services.get(index), console);
                return;
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi client: " + e.getMessage());
        }
    }

    private static List<Service> discover(NetworkInterface network, int timeout, int udpPort) throws IOException {
        Map<String, Service> unique = new LinkedHashMap<>();
        int duplicates = 0, invalid = 0;
        try (MulticastSocket socket = new MulticastSocket()) {
            socket.setNetworkInterface(network);
            socket.setTimeToLive(1);
            socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
            byte[] query = "DISCOVER_SERVICE".getBytes(StandardCharsets.UTF_8);
            InetAddress group = InetAddress.getByName(DiscoveryProtocol.GROUP);
            long start = System.nanoTime();
            long deadline = start + timeout * 1_000_000L;
            long nextSend = start;
            int sent = 0;
            System.out.println("DISCOVERING group=" + group.getHostAddress() + ":" + udpPort
                    + "; interface=" + network.getName() + "; timeout=" + timeout + " ms");
            while (System.nanoTime() < deadline) {
                long now = System.nanoTime();
                // Gui toi da 3 lan trong cung cua so timeout, khong keo dai deadline.
                if (sent < 3 && now >= nextSend) {
                    socket.send(new DatagramPacket(query, query.length, group, udpPort));
                    sent++;
                    nextSend = start + sent * timeout * 1_000_000L / 3;
                }
                long wake = sent < 3 ? Math.min(deadline, nextSend) : deadline;
                long remaining = wake - System.nanoTime();
                if (remaining <= 0) continue;
                socket.setSoTimeout((int) Math.max(1, (remaining + 999_999) / 1_000_000));
                DatagramPacket packet = new DatagramPacket(new byte[DiscoveryProtocol.MAX_PACKET + 1], DiscoveryProtocol.MAX_PACKET + 1);
                try { socket.receive(packet); }
                catch (SocketTimeoutException e) { continue; }
                if (packet.getLength() > DiscoveryProtocol.MAX_PACKET) { invalid++; continue; }
                String response;
                try {
                    response = StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(
                            packet.getData(), packet.getOffset(), packet.getLength())).toString();
                } catch (java.nio.charset.CharacterCodingException e) { invalid++; continue; }
                String[] parts = response.split(" ", -1);
                if (parts.length != 4 || !parts[0].equals("SERVICE")
                        || !DiscoveryProtocol.token(parts[1]) || !DiscoveryProtocol.token(parts[3])) {
                    invalid++; continue;
                }
                int port;
                try { port = DiscoveryProtocol.port(parts[2]); }
                catch (IllegalArgumentException e) { invalid++; continue; }
                // Dia chi TCP lay tu NGUON datagram, khong lay tu payload hay dia chi multicast.
                Service service = new Service(packet.getAddress(), port, parts[1], parts[3]);
                String key = service.address.getHostAddress() + ":" + port + ":" + service.name + ":" + service.version;
                if (unique.putIfAbsent(key, service) != null) duplicates++;
            }
        }
        System.out.println("DISCOVERY_DONE duplicates=" + duplicates + "; invalid=" + invalid);
        List<Service> services = new ArrayList<>(unique.values());
        services.sort(Comparator.comparing((Service s) -> s.address.getHostAddress()).thenComparingInt(s -> s.port)
                .thenComparing(s -> s.name).thenComparing(s -> s.version));
        return services;
    }

    private static void connect(Service service, BufferedReader console) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(service.address, service.port), 3000);
            socket.setSoTimeout(5000);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            System.out.println("TCP_CONNECTED " + service.address.getHostAddress() + ":" + service.port);
            String greeting = input.readLine();
            if (greeting == null) { System.out.println("Server đã đóng kết nối."); return; }
            System.out.println("Server: " + greeting);
            if (!greeting.startsWith("WELCOME ")) return;
            System.out.println("Nhập TIME, ECHO <nội dung>, QUIT:");
            String command;
            while ((command = console.readLine()) != null) {
                output.write(command);
                output.newLine();
                output.flush();
                String response = input.readLine();
                if (response == null) { System.out.println("Server đã đóng kết nối."); return; }
                System.out.println("Server: " + response);
                if (command.equals("QUIT")) return;
            }
        }
    }
}
