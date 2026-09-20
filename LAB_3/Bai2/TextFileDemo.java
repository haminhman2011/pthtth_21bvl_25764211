package Bai2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextFileDemo {
    public static void main(String[] args) {
        Path file = Path.of("data", "ghi_chu.txt");

        try {
            // Tạo thư mục "data" nếu chưa tồn tại
            Files.createDirectories(file.getParent());

            // Ghi ba dòng tiếng Việt vào file (chế độ ghi mới, ghi đè)
            try (BufferedWriter writer = Files.newBufferedWriter(
                    file, StandardCharsets.UTF_8)) {
                writer.write("Java I/O làm việc với các luồng dữ liệu.");
                writer.newLine();
                writer.write("BufferedWriter giúp ghi văn bản hiệu quả.");
                writer.newLine();
                writer.write("UTF-8 hỗ trợ tiếng Việt ổn định.");
                writer.newLine();
            }

            // Đọc lại từng dòng và đánh số thứ tự
            try (BufferedReader reader = Files.newBufferedReader(
                    file, StandardCharsets.UTF_8)) {
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