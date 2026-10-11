package Bai_5.Bai_8;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class FileClient {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            System.err.println("Cách chạy: java Bai_5.Bai_8.FileClient <file> [host] [port]");
            return;
        }
        try {
            Path file = Paths.get(args[0]);
            if (!Files.isRegularFile(file)) throw new IOException("Đường dẫn không phải file thường");
            long size = Files.size(file);
            if (size > FileProtocol.MAX_FILE_SIZE) throw new IOException("File vượt giới hạn 1 GiB");
            String name = file.getFileName().toString();
            if (name.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > FileProtocol.MAX_NAME_BYTES)
                throw new IOException("Tên file quá dài");
            String hash = FileProtocol.hex(FileProtocol.hash(file));
            int port = args.length > 2 ? Integer.parseInt(args[2]) : 5008;
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Port phải từ 1 đến 65535");
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(args.length > 1 ? args[1] : "localhost", port), 3000);
                socket.setSoTimeout(10_000);
                DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                FileProtocol.writeString(output, name);
                output.writeLong(size);
                FileProtocol.writeString(output, hash);
                output.flush();
                String response = FileProtocol.readString(input, 1024);
                if (!response.equals("READY")) {
                    System.out.println("Server: " + response);
                    return;
                }
                try (InputStream source = Files.newInputStream(file)) {
                    byte[] buffer = new byte[8192];
                    long remaining = size;
                    while (remaining > 0) {
                        int length = source.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                        if (length < 0) throw new EOFException("File bị thay đổi hoặc thu ngắn trong khi gửi");
                        output.write(buffer, 0, length);
                        remaining -= length;
                    }
                }
                output.flush();
                // Khong shutdownOutput: server phai biet ranh gioi bang size, ke ca file rong.
                System.out.println("File: " + name + "; size: " + size + "; SHA-256: " + hash);
                System.out.println("Server: " + FileProtocol.readString(input, 1024));
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Hết thời gian chờ 10 giây.");
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi client: " + e.getMessage());
        }
    }
}
