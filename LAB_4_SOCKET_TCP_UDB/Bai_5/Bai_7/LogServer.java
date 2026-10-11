package Bai_5.Bai_7;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class LogServer {
    private static final int MAX_CLIENTS = 32;
    // Khoa theo nhom co dinh: cung clientId luon dung cung khoa, khong tang bo nho theo ID.
    private final Object[] fileLocks = new Object[64];
    private final Path logDirectory;
    private final Semaphore slots = new Semaphore(MAX_CLIENTS);
    private final ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);
    private final java.util.Set<Socket> active = ConcurrentHashMap.newKeySet();

    private LogServer(Path directory) throws IOException {
        logDirectory = directory.toAbsolutePath().normalize();
        Files.createDirectories(logDirectory);
        for (int i = 0; i < fileLocks.length; i++) fileLocks[i] = new Object();
    }

    public static void main(String[] args) {
        if (args.length > 2) {
            System.err.println("Cách chạy: java Bai_5.Bai_7.LogServer [port] [thư_mục_log]");
            return;
        }
        try {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : 5007;
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Port phải từ 1 đến 65535");
            Path directory = Paths.get(args.length > 1 ? args[1] : "Bai_5/Bai_7/data/logs");
            new LogServer(directory).run(port);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        }
    }

    private void run(int port) throws IOException {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Log server tại cổng " + port + "; logs: " + logDirectory);
            while (true) {
                Socket socket = server.accept();
                if (!slots.tryAcquire()) {
                    try (Socket rejected = socket) {
                        BufferedWriter output = writer(rejected);
                        reply(output, "ERR SERVER_FULL");
                    } catch (IOException e) {
                        System.err.println("Không gửi được lỗi server đầy: " + e.getMessage());
                    }
                    continue;
                }
                active.add(socket);
                pool.execute(() -> {
                    try (Socket client = socket) {
                        serve(client);
                    } catch (IOException e) {
                        System.err.println("Lỗi phiên client: " + e.getMessage());
                    } finally {
                        active.remove(socket);
                        slots.release();
                    }
                });
            }
        } finally {
            pool.shutdownNow();
            for (Socket socket : active) {
                try { socket.close(); } catch (IOException ignored) { }
            }
        }
    }

    private static BufferedWriter writer(Socket socket) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    private static void reply(BufferedWriter output, String response) throws IOException {
        output.write(response);
        output.newLine();
        output.flush();
    }

    private void serve(Socket socket) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter output = writer(socket);
        socket.setSoTimeout(60_000);
        String clientId = null;
        String message;
        while ((message = input.readLine()) != null) {
            if (message.equals("QUIT")) {
                reply(output, "BYE");
                return;
            }
            if (clientId == null) {
                if (!message.startsWith("HELLO ")) {
                    reply(output, "ERR HELLO_REQUIRED");
                    continue;
                }
                String candidate = message.substring(6);
                if (!candidate.matches("[A-Za-z0-9_-]+")) {
                    reply(output, "ERR INVALID_CLIENT_ID");
                    continue;
                }
                clientId = candidate;
                socket.setSoTimeout(0);
                reply(output, "OK HELLO " + clientId);
                continue;
            }
            try {
                append(clientId, socket.getRemoteSocketAddress().toString(), message);
                reply(output, "OK LOGGED");
            } catch (IOException | InvalidPathException e) {
                System.err.println("Không ghi được log cho " + clientId + ": " + e.getMessage());
                reply(output, "ERR LOG_WRITE");
            }
        }
    }

    private void append(String clientId, String remote, String content) throws IOException {
        Path file = logDirectory.resolve(clientId + ".txt").normalize();
        if (!file.getParent().equals(logDirectory)) throw new IOException("Đường dẫn log không hợp lệ");
        // Case-insensitive lock key de cac ID khac hoa/thuong van an toan tren Windows.
        int index = Math.floorMod(clientId.toLowerCase(java.util.Locale.ROOT).hashCode(), fileLocks.length);
        synchronized (fileLocks[index]) {
            String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            // Escape tab va backslash de moi ban ghi luon co dung ba cot TSV.
            String escaped = content.replace("\\", "\\\\").replace("\t", "\\t");
            String entry = timestamp + "\t" + remote + "\t" + escaped + System.lineSeparator();
            try (BufferedWriter output = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND, LinkOption.NOFOLLOW_LINKS)) {
                output.write(entry);
            }
        }
    }
}
