package Bai_5.Bai_3;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UdpDateTimeServer {
    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Cách chạy: java Bai_5.Bai_3.UdpDateTimeServer [port]");
            return;
        }
        try (DatagramSocket socket = new DatagramSocket(DateTimeService.port(args, 0, 5003))) {
            System.out.println("UDP server đang lắng nghe tại cổng " + socket.getLocalPort());
            byte[] buffer = new byte[65507];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                String command = new String(request.getData(), request.getOffset(), request.getLength(), StandardCharsets.UTF_8);
                byte[] response = DateTimeService.respond(command).getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(response, response.length, request.getAddress(), request.getPort()));
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi UDP server: " + e.getMessage());
        }
    }
}
