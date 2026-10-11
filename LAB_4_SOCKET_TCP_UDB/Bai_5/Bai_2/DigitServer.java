package Bai_5.Bai_2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class DigitServer {
    private static final String[] WORDS = {
        "không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"
    };

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Cách chạy: java Bai_5.Bai_2.DigitServer [port]");
            return;
        }
        try {
            int port = args.length == 1 ? Integer.parseInt(args[0]) : 5002;
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Port phải từ 1 đến 65535");
            }
            try (ServerSocket server = new ServerSocket(port)) {
                System.out.println("Server đang lắng nghe tại cổng " + port);
                while (true) {
                    try (Socket socket = server.accept()) {
                        serve(socket);
                    } catch (IOException e) {
                        System.err.println("Lỗi kết nối client: " + e.getMessage());
                    }
                }
            }
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Không chạy được server: " + e.getMessage());
        }
    }

    private static void serve(Socket socket) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(
                socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                socket.getOutputStream(), StandardCharsets.UTF_8));
        String request;
        while ((request = input.readLine()) != null) {
            boolean quit = request.equals("QUIT");
            String response;
            if (quit) {
                response = "BYE";
            } else if (request.length() == 1 && request.charAt(0) >= '0'
                    && request.charAt(0) <= '9') {
                response = WORDS[request.charAt(0) - '0'];
            } else {
                response = "ERR INVALID_DIGIT";
            }
            output.write(response);
            output.newLine();
            output.flush();
            if (quit) {
                return;
            }
        }
    }
}
