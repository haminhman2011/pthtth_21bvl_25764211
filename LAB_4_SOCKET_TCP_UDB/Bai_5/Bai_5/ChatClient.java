package Bai_5.Bai_5;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ChatClient {
    public static void main(String[] args) {
        if (args.length > 2) {
            System.err.println("Cách chạy: java Bai_5.Bai_5.ChatClient [host] [port]");
            return;
        }
        try (Socket socket = new Socket()) {
            int port = args.length == 2 ? Integer.parseInt(args[1]) : 5005;
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Port phải từ 1 đến 65535");
            socket.connect(new InetSocketAddress(args.length > 0 ? args[0] : "localhost", port), 3000);
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            // Thread nhap rieng de tin nhan den hien ngay ca khi dang cho ban phim.
            Thread sender = new Thread(() -> {
                try {
                    String command;
                    while ((command = console.readLine()) != null) {
                        output.write(command);
                        output.newLine();
                        output.flush();
                        if (command.equals("QUIT")) return;
                    }
                    socket.shutdownOutput();
                } catch (IOException e) {
                    try { socket.close(); } catch (IOException ignored) { }
                }
            }, "chat-console");
            sender.setDaemon(true);
            System.out.println("Chọn nickname: NICK <tên>. Sau đó dùng USERS, MSG <nội dung>, QUIT.");
            sender.start();
            String message;
            while ((message = input.readLine()) != null) {
                System.out.println(message);
                if (message.equals("BYE")) return;
            }
            System.out.println("Server đã đóng kết nối.");
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi client: " + e.getMessage());
        }
    }
}
