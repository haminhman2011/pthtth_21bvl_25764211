package Bai_5.Bai_4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class CalculatorClient {
    public static void main(String[] args) {
        if (args.length > 2) {
            System.err.println("Cách chạy: java Bai_5.Bai_4.CalculatorClient [host] [port]");
            return;
        }
        try (Socket socket = new Socket()) {
            String host = args.length > 0 ? args[0] : "localhost";
            int port = args.length == 2 ? Integer.parseInt(args[1]) : 5004;
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Port phải từ 1 đến 65535");
            }
            socket.connect(new InetSocketAddress(host, port), 3000);
            socket.setSoTimeout(3000);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            System.out.println("Nhập CALC <+|-|*|/> <số 1> <số 2>. Nhập QUIT để thoát client.");
            String request;
            while ((request = console.readLine()) != null) {
                // QUIT la lenh cuc bo: dong socket, khong gui den server.
                if (request.equals("QUIT")) return;
                output.write(request);
                output.newLine();
                output.flush();
                String response = input.readLine();
                if (response == null) {
                    System.out.println("Server đã đóng kết nối.");
                    return;
                }
                System.out.println("Server: " + response);
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Hết thời gian chờ 3 giây; kết thúc kết nối.");
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi client: " + e.getMessage());
        }
    }
}
