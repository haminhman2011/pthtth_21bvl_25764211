package Bai_5.Bai_4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class CalculatorServer {
    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Cách chạy: java Bai_5.Bai_4.CalculatorServer [port]");
            return;
        }
        try {
            int port = args.length == 1 ? Integer.parseInt(args[0]) : 5004;
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Port phải từ 1 đến 65535");
            }
            try (ServerSocket server = new ServerSocket(port)) {
                System.out.println("Calculator server đang lắng nghe tại cổng " + port);
                while (true) {
                    try (Socket client = server.accept()) {
                        serve(client);
                    } catch (IOException e) {
                        System.err.println("Lỗi phiên client: " + e.getMessage());
                    }
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        }
    }

    private static void serve(Socket client) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(
                client.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                client.getOutputStream(), StandardCharsets.UTF_8));
        String request;
        while ((request = input.readLine()) != null) {
            output.write(Calculator.calculate(request));
            output.newLine();
            output.flush();
        }
    }
}
