package Bai_5.Bai_3;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TcpDateTimeServer {
    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Cách chạy: java Bai_5.Bai_3.TcpDateTimeServer [port]");
            return;
        }
        try (ServerSocket server = new ServerSocket(DateTimeService.port(args, 0, 5003))) {
            System.out.println("TCP server đang lắng nghe tại cổng " + server.getLocalPort());
            while (true) {
                try (Socket client = server.accept()) {
                    serve(client);
                } catch (IOException e) {
                    System.err.println("Lỗi phiên TCP: " + e.getMessage());
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi TCP server: " + e.getMessage());
        }
    }

    private static void serve(Socket client) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));
        String command;
        while ((command = input.readLine()) != null) {
            boolean quit = command.equals("QUIT");
            output.write(quit ? "BYE" : DateTimeService.respond(command));
            output.newLine();
            output.flush();
            if (quit) return;
        }
    }
}
