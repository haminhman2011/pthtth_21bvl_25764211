import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryManager {
    private final List<Product> products = new ArrayList<>();

    // ------- Thao tác trên danh sách -------

    public void add(Product p) {
        products.add(p);
    }

    public List<Product> getAll() {
        return List.copyOf(products);   // trả bản sao chỉ đọc, bảo vệ dữ liệu
    }

    public int size() {
        return products.size();
    }

    public void clear() {
        products.clear();
    }

    /** (Chức năng 7) Tổng giá trị tồn kho. */
    public double totalInventoryValue() {
        double total = 0;
        for (Product p : products) {
            total += p.inventoryValue();
        }
        return total;
    }

    /** (Chức năng 8) Sản phẩm có giá trị tồn kho cao nhất. */
    public Optional<Product> maxInventoryValue() {
        Product max = null;
        for (Product p : products) {
            if (max == null || p.inventoryValue() > max.inventoryValue()) {
                max = p;
            }
        }
        return Optional.ofNullable(max);
    }

    // ------- I/O streams -------

    public void saveToCsv(Path output) throws IOException {
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        // (14) try-with-resources ; (12)(13) newBufferedWriter + UTF_8
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writer.write("ma,ten,donGia,soLuong");
            writer.newLine();
            for (Product p : products) {
                writer.write(p.toCsvRow());
                writer.newLine();
            }
        }
    }

    public int loadFromCsv(Path input) throws IOException {
        products.clear();
        int errorCount = 0;
        // (14) try-with-resources ; (12)(13) newBufferedReader + UTF_8
        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            reader.readLine(); // bỏ qua dòng tiêu đề
            String line;
            int lineNumber = 1;
            // (15) điều khiển vòng lặp bằng readLine() != null
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length != 4) {   // dòng thiếu/thừa cột
                    System.err.println("[" + input + "] Bỏ qua dòng " + lineNumber
                            + ": có " + parts.length + " cột (cần 4)");
                    errorCount++;
                    continue;
                }
                try {
                    double price = Double.parseDouble(parts[2].trim());
                    int qty = Integer.parseInt(parts[3].trim());
                    products.add(new Product(parts[0].trim(), parts[1].trim(), price, qty));
                } catch (NumberFormatException e) {   // đơn giá / số lượng không phải số
                    System.err.println("[" + input + "] Dòng " + lineNumber
                            + ": số không hợp lệ (" + e.getMessage() + ")");
                    errorCount++;
                } catch (IllegalArgumentException e) { // vi phạm ràng buộc Product
                    System.err.println("[" + input + "] Dòng " + lineNumber
                            + ": " + e.getMessage());
                    errorCount++;
                }
            }
        }
        return errorCount;
    }

    public void writeReport(Path report) throws IOException {
        if (report.getParent() != null) {
            Files.createDirectories(report.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(report, StandardCharsets.UTF_8)) {
            writer.write("===== BÁO CÁO TỒN KHO =====");
            writer.newLine();
            writer.write("Số sản phẩm: " + size());
            writer.newLine();
            writer.write("Tổng giá trị tồn kho: %,.0f VND".formatted(totalInventoryValue()));
            writer.newLine();
            writer.newLine();

            writer.write("Chi tiết:");
            writer.newLine();
            for (Product p : products) {
                writer.write(p.toString());
                writer.newLine();
            }

            writer.newLine();
            maxInventoryValue().ifPresentOrElse(
                p -> {
                    try {
                        writer.write("Sản phẩm có giá trị tồn kho cao nhất: "
                                + p.getCode() + " - " + p.getName()
                                + " (%,.0f VND)".formatted(p.inventoryValue()));
                        writer.newLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> { /* danh sách rỗng, không ghi gì */ }
            );
        }
    }
}
