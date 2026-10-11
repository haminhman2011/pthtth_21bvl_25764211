package Bai_5.Bai_7;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class LogClient {
    public static void main(String[] args) {
        if (args.length > 2) {
            System.err.println("Cách chạy: java Bai_5.Bai_7.LogClient [host] [port]");
            return;
        }
        try (Socket socket = new Socket()) {
            int port = args.length > 1 ? Integer.parseInt(args[1]) : 5007;
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Port phải từ 1 đến 65535");
            socket.connect(new InetSocketAddress(args.length > 0 ? args[0] : "localhost", port), 3000);
            socket.setSoTimeout(5000);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            System.out.println("Gửi HELLO <clientId> trước, sau đó nhập nội dung. Nhập QUIT để kết thúc.");
            String message;
            while ((message = console.readLine()) != null) {
                output.write(message);
                output.newLine();
                output.flush();
                String response = input.readLine();
                if (response == null) {
                    System.out.println("Server đã đóng kết nối.");
                    return;
                }
                System.out.println("Server: " + response);
                if (message.equals("QUIT")) return;
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Hết thời gian chờ phản hồi 5 giây; đóng kết nối.");
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi client: " + e.getMessage());
        }
    }
}
