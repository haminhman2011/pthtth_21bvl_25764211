package Bai_5.Bai_5;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;

public class ChatServer {
    private static final int MAX_CLIENTS = 32;
    private final ConcurrentHashMap<String, Session> users = new ConcurrentHashMap<>();
    private final Semaphore slots = new Semaphore(MAX_CLIENTS);
    private final ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Cách chạy: java Bai_5.Bai_5.ChatServer [port]");
            return;
        }
        try {
            int port = args.length == 1 ? Integer.parseInt(args[0]) : 5005;
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Port phải từ 1 đến 65535");
            new ChatServer().run(port);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        }
    }

    private void run(int port) throws IOException {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Chat server đang lắng nghe tại cổng " + port);
            while (true) {
                Socket socket = server.accept();
                if (!slots.tryAcquire()) {
                    try (Socket rejected = socket) {
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                                rejected.getOutputStream(), StandardCharsets.UTF_8));
                        writer.write("ERR SERVER_FULL\n");
                        writer.flush();
                    } catch (IOException e) {
                        System.err.println("Không gửi được thông báo server đầy: " + e.getMessage());
                    }
                    continue;
                }
                pool.execute(() -> {
                    try (Socket client = socket) {
                        new Session(client).serve();
                    } catch (IOException e) {
                        System.err.println("Client ngắt kết nối: " + e.getMessage());
                    } finally {
                        slots.release();
                    }
                });
            }
        } finally {
            pool.shutdownNow();
            for (Session session : users.values()) session.disconnect();
        }
    }

    private class Session {
        private final Socket socket;
        private final BufferedReader input;
        private final BufferedWriter output;
        private String nickname;

        Session(Socket socket) throws IOException {
            this.socket = socket;
            input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }

        // Nhieu thread co the gui cho cung mot client; khoa tron ven moi dong.
        synchronized void send(String message) throws IOException {
            output.write(message);
            output.newLine();
            output.flush();
        }

        void disconnect() {
            if (nickname != null) users.remove(nickname, this);
            try { socket.close(); } catch (IOException ignored) { }
        }

        void serve() throws IOException {
            try {
                // Tranh client chua chon nickname giu cho trong pool vo han.
                socket.setSoTimeout(60_000);
                send("NICK_REQUIRED");
                String request;
                while ((request = input.readLine()) != null) {
                    if (request.equals("QUIT")) {
                        send("BYE");
                        return;
                    }
                    if (nickname == null) {
                        if (!request.startsWith("NICK ")) {
                            send("ERR NICK_REQUIRED");
                            continue;
                        }
                        String candidate = request.substring(5);
                        if (!candidate.matches("[A-Za-z0-9_]{1,20}")) {
                            send("ERR INVALID_NICK");
                            continue;
                        }
                        // Gan truoc putIfAbsent de phien da dang ky luon co nickname.
                        nickname = candidate;
                        if (users.putIfAbsent(candidate, this) != null) {
                            nickname = null;
                            send("ERR NICK_IN_USE");
                        } else {
                            send("OK NICK " + nickname);
                            socket.setSoTimeout(0);
                        }
                    } else if (request.equals("USERS")) {
                        ArrayList<String> names = new ArrayList<>(users.keySet());
                        Collections.sort(names);
                        send("USERS " + String.join(" ", names));
                    } else if (request.startsWith("MSG ")) {
                        String text = request.substring(4);
                        if (text.isBlank()) {
                            send("ERR EMPTY_MESSAGE");
                            continue;
                        }
                        String message = "MSG " + nickname + " " + text;
                        for (Session recipient : users.values()) {
                            if (recipient == this) continue;
                            try {
                                recipient.send(message);
                            } catch (IOException e) {
                                recipient.disconnect();
                            }
                        }
                        send("OK MSG");
                    } else {
                        send("ERR UNKNOWN_COMMAND");
                    }
                }
            } finally {
                disconnect();
            }
        }
    }
}
