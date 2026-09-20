package Bai2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TextFileDemoExtended {
    public static void main(String[] args) {
        Path file = Path.of("data", "ghi_chu.txt");

        try {
            Files.createDirectories(file.getParent());

            // ---- Nhiệm vụ 1: ghi mới bằng CREATE, sau đó nối thêm bằng APPEND ----

            // Lần ghi đầu: tạo file (nếu chưa có) và ghi 3 dòng gốc.
            // TRUNCATE_EXISTING để nếu file đã tồn tại thì xoá nội dung cũ, tránh lẫn dữ liệu.
            try (BufferedWriter writer = Files.newBufferedWriter(
                    file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.write("Java I/O làm việc với các luồng dữ liệu.");
                writer.newLine();
                writer.write("BufferedWriter giúp ghi văn bản hiệu quả.");
                writer.newLine();
                writer.write("UTF-8 hỗ trợ tiếng Việt ổn định.");
                writer.newLine();
            }

            // Nối thêm 2 dòng mà không xoá nội dung cũ nhờ APPEND
            try (BufferedWriter writer = Files.newBufferedWriter(
                    file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                writer.write("Dòng được nối thêm bằng APPEND.");
                writer.newLine();
                writer.write("Nội dung cũ vẫn được giữ nguyên.");
                writer.newLine();
            }

            // ---- Nhiệm vụ 2: in đường dẫn tuyệt đối của tệp ----
            System.out.println("Đường dẫn tuyệt đối: " + file.toAbsolutePath());
            System.out.println();

            // Đọc lại và đánh số để kiểm tra kết quả (đọc đúng bằng UTF-8)
            System.out.println("=== Đọc bằng UTF-8 (đúng charset) ===");
            try (BufferedReader reader = Files.newBufferedReader(
                    file, StandardCharsets.UTF_8)) {
                String line;
                int number = 1;
                while ((line = reader.readLine()) != null) {
                    System.out.printf("%d. %s%n", number++, line);
                }
            }

            // ---- Nhiệm vụ 3: thử đọc cùng tệp bằng charset khác ----
            // File được ghi bằng UTF-8, giờ cố tình đọc bằng ISO-8859-1 (Latin-1).
            // Mỗi byte bị hiểu là 1 ký tự Latin-1 => các ký tự tiếng Việt (2-3 byte trong UTF-8)
            // sẽ hiển thị sai thành các ký tự lạ. Đây là minh hoạ vì sao charset khi ghi và đọc
            // phải khớp nhau.
            System.out.println();
            System.out.println("=== Đọc lại bằng ISO-8859-1 (sai charset) ===");
            try (BufferedReader reader = Files.newBufferedReader(
                    file, Charset.forName("ISO-8859-1"))) {
                String line;
                int number = 1;
                while ((line = reader.readLine()) != null) {
                    System.out.printf("%d. %s%n", number++, line);
                }
            }

        } catch (IOException e) {
            System.err.println("Lỗi xử lý tệp " + file + ": " + e.getMessage());
        }
    }
}
