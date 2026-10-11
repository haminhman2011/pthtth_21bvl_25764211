package Bai_5.Bai_2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class DigitClient {
    public static void main(String[] args) {
        if (args.length > 2) {
            System.err.println("Cách chạy: java Bai_5.Bai_2.DigitClient [host] [port]");
            return;
        }
        String host = args.length >= 1 ? args[0] : "localhost";
        try {
            int port = args.length == 2 ? Integer.parseInt(args[1]) : 5002;
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Port phải từ 1 đến 65535");
            }
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 5000);
                socket.setSoTimeout(5000);
                BufferedReader console = new BufferedReader(new InputStreamReader(
                        System.in, StandardCharsets.UTF_8));
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream(), StandardCharsets.UTF_8));
                System.out.println("Nhập một chữ số 0–9, hoặc QUIT để thoát:");
                String request;
                while ((request = console.readLine()) != null) {
                    // Giu nguyen khoang trang de server kiem tra dung du lieu goc.
                    output.write(request);
                    output.newLine();
                    output.flush();
                    String response = input.readLine();
                    if (response == null) {
                        System.out.println("Server đã đóng kết nối.");
                        break;
                    }
                    System.out.println("Server: " + response);
                    if (request.equals("QUIT")) {
                        break;
                    }
                }
            }
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Lỗi client: " + e.getMessage());
        }
    }
}
