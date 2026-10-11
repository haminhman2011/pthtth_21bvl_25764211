package Bai_5.Bai_8;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.Locale;

public class FileServer {
    private static final Path UPLOAD = Paths.get("Bai_5/Bai_8/upload").toAbsolutePath().normalize();
    private static final int TIMEOUT_MS = 10_000;

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Cách chạy: java Bai_5.Bai_8.FileServer [port]");
            return;
        }
        try {
            int port = args.length == 1 ? Integer.parseInt(args[0]) : 5008;
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Port phải từ 1 đến 65535");
            Files.createDirectories(UPLOAD);
            if (Files.isSymbolicLink(UPLOAD)) throw new IOException("Thư mục upload không được là symbolic link");
            try (ServerSocket server = new ServerSocket(port)) {
                System.out.println("File server tại cổng " + port + "; upload: " + UPLOAD);
                while (true) {
                    try (Socket socket = server.accept()) {
                        socket.setSoTimeout(TIMEOUT_MS);
                        receive(socket);
                    } catch (IOException e) {
                        System.err.println("Lỗi phiên upload: " + e.getMessage());
                    }
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        }
    }

    private static boolean validName(String name) {
        if (name.isBlank() || name.equals(".") || name.equals("..")
                || name.endsWith(".") || name.endsWith(" ")) return false;
        // Tu choi ca duong dan Unix, Windows, ADS va ky tu dieu khien.
        for (char c : name.toCharArray()) {
            if (Character.isISOControl(c) || "<>:\"/\\|?*".indexOf(c) >= 0) return false;
        }
        String base = name.split("\\.", 2)[0].toUpperCase(Locale.ROOT);
        return !base.matches("CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]");
    }

    private static void reply(DataOutputStream output, String text) throws IOException {
        FileProtocol.writeString(output, text);
        output.flush();
    }

    private static void receive(Socket socket) throws IOException {
        DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        Path temporary = null;
        try {
            String name;
            long size;
            String expectedHash;
            try {
                name = FileProtocol.readString(input, FileProtocol.MAX_NAME_BYTES);
                size = input.readLong();
                expectedHash = FileProtocol.readString(input, 64);
            } catch (SocketTimeoutException e) {
                reply(output, "ERR TIMEOUT");
                return;
            } catch (IOException e) {
                reply(output, "ERR INVALID_METADATA");
                return;
            }
            if (!validName(name)) {
                reply(output, "ERR INVALID_FILENAME");
                return;
            }
            if (size < 0 || size > FileProtocol.MAX_FILE_SIZE) {
                reply(output, "ERR INVALID_SIZE");
                return;
            }
            if (!expectedHash.matches("[0-9a-fA-F]{64}")) {
                reply(output, "ERR INVALID_HASH");
                return;
            }
            Path destination = UPLOAD.resolve(name).normalize();
            if (!destination.getParent().equals(UPLOAD)) {
                reply(output, "ERR INVALID_FILENAME");
                return;
            }
            if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
                reply(output, "ERR FILE_EXISTS");
                return;
            }
            temporary = Files.createTempFile(UPLOAD, ".receiving-", ".part");
            reply(output, "READY");
            try (OutputStream file = Files.newOutputStream(temporary)) {
                byte[] buffer = new byte[8192];
                long remaining = size;
                while (remaining > 0) {
                    int length = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                    if (length == -1) {
                        reply(output, "ERR INCOMPLETE_FILE");
                        return;
                    }
                    file.write(buffer, 0, length);
                    remaining -= length;
                }
            } catch (SocketTimeoutException e) {
                reply(output, "ERR TIMEOUT");
                return;
            }
            // Doc lai file da luu, khong chi hash buffer tren mang.
            byte[] actualHash = FileProtocol.hash(temporary);
            byte[] expected = java.util.HexFormat.of().parseHex(expectedHash);
            if (!MessageDigest.isEqual(actualHash, expected)) {
                reply(output, "ERR HASH_MISMATCH");
                return;
            }
            // Khong REPLACE_EXISTING: file da ton tai khong bi ghi de.
            Files.move(temporary, destination);
            temporary = null;
            reply(output, "OK");
        } catch (FileAlreadyExistsException e) {
            reply(output, "ERR FILE_EXISTS");
        } catch (InvalidPathException e) {
            reply(output, "ERR INVALID_FILENAME");
        } catch (IOException e) {
            try { reply(output, "ERR IO_ERROR"); } catch (IOException ignored) { }
            throw e;
        } finally {
            if (temporary != null) Files.deleteIfExists(temporary);
        }
    }
}
