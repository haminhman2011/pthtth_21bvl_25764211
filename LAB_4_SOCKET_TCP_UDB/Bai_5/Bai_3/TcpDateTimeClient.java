package Bai_5.Bai_3;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TcpDateTimeClient {
    public static void main(String[] args) {
        if (args.length > 2) {
            System.err.println("Cách chạy: java Bai_5.Bai_3.TcpDateTimeClient [host] [port]");
            return;
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(args.length > 0 ? args[0] : "localhost",
                    DateTimeService.port(args, 1, 5003)), 3000);
            socket.setSoTimeout(3000);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            System.out.println("TCP: Nhập DATE, TIME, DATETIME hoặc QUIT:");
            String command;
            while ((command = console.readLine()) != null) {
                output.write(command);
                output.newLine();
                output.flush();
                String response = input.readLine();
                if (response == null) {
                    System.out.println("TCP: Server đã đóng kết nối.");
                    return;
                }
                System.out.println("Server: " + response);
                if (command.equals("QUIT")) return;
            }
        } catch (SocketTimeoutException e) {
            System.err.println("TCP: Hết thời gian chờ 3 giây; kết thúc kết nối.");
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("TCP: Lỗi kết nối: " + e.getMessage());
        }
    }
}
