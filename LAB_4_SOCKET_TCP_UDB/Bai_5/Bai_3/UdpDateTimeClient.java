package Bai_5.Bai_3;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UdpDateTimeClient {
    public static void main(String[] args) {
        if (args.length > 2) {
            System.err.println("Cách chạy: java Bai_5.Bai_3.UdpDateTimeClient [host] [port]");
            return;
        }
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress host = InetAddress.getByName(args.length > 0 ? args[0] : "localhost");
            int port = DateTimeService.port(args, 1, 5003);
            socket.setSoTimeout(3000);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            System.out.println("UDP: Nhập DATE, TIME hoặc DATETIME. Nhấn Ctrl+C để thoát (không dùng QUIT).");
            String command;
            while ((command = console.readLine()) != null) {
                byte[] data = command.getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(data, data.length, host, port));
                long deadline = System.nanoTime() + 3_000_000_000L;
                try {
                    // Chi nhan phan hoi tu dia chi va cong server da chon.
                    while (true) {
                        long remaining = deadline - System.nanoTime();
                        if (remaining <= 0) throw new SocketTimeoutException();
                        socket.setSoTimeout((int) Math.max(1, remaining / 1_000_000));
                        DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
                        socket.receive(response);
                        if (response.getAddress().equals(host) && response.getPort() == port) {
                            System.out.println("Server: " + new String(response.getData(), response.getOffset(), response.getLength(), StandardCharsets.UTF_8));
                            break;
                        }
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("UDP: Hết thời gian chờ 3 giây; không nhận được phản hồi. Có thể gửi lệnh tiếp.");
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("UDP: Lỗi: " + e.getMessage());
        }
    }
}
