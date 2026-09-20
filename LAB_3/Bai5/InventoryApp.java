import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

public class InventoryApp {
    private static final Path CSV_FILE    = Path.of("data", "inventory.csv");
    private static final Path REPORT_FILE = Path.of("data", "inventory-report.txt");

    private final InventoryManager manager = new InventoryManager();
    // (13)(14) reader bọc System.in bằng InputStreamReader chỉ rõ UTF-8
    private final BufferedReader keyboard = new BufferedReader(
            new InputStreamReader(System.in, StandardCharsets.UTF_8));

    public static void main(String[] args) {
        new InventoryApp().run();
    }

    private void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine("Chọn: ");
            if (choice == null) break;   // gặp EOF thì thoát

            switch (choice.trim()) {
                case "1" -> inputProducts();
                case "2" -> saveCsv();
                case "3" -> loadCsv();
                case "4" -> showAllAndTotal();
                case "5" -> showMax();
                case "6" -> writeReport();
                case "0" -> running = false;
                default  -> System.out.println("Lựa chọn không hợp lệ.");
            }
        }
        System.out.println("Kết thúc chương trình.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("========== QUẢN LÝ TỒN KHO ==========");
        System.out.println("1. Nhập sản phẩm từ bàn phím");
        System.out.println("2. Lưu danh sách vào " + CSV_FILE);
        System.out.println("3. Đọc lại danh sách từ " + CSV_FILE);
        System.out.println("4. Hiển thị toàn bộ + tổng giá trị tồn kho");
        System.out.println("5. Sản phẩm có giá trị tồn kho cao nhất");
        System.out.println("6. Ghi báo cáo " + REPORT_FILE);
        System.out.println("0. Thoát");
        System.out.println("=====================================");
    }

    // (Chức năng 4) Nhập danh sách sản phẩm từ bàn phím, có kiểm tra hợp lệ
    private void inputProducts() {
        int n;
        try {
            n = Integer.parseInt(readLine("Số sản phẩm cần nhập: ").trim());
        } catch (NumberFormatException e) {
            System.out.println("Số lượng nhập phải là số nguyên. Hủy.");
            return;
        }

        int added = 0;
        for (int i = 1; i <= n; i++) {
            System.out.println("-- Sản phẩm " + i + " --");
            String code = readLine("  Mã: ");
            String name = readLine("  Tên: ");
            String priceStr = readLine("  Đơn giá: ");
            String qtyStr = readLine("  Số lượng: ");
            if (code == null || name == null || priceStr == null || qtyStr == null) {
                System.out.println("  Gặp EOF, dừng nhập.");
                break;
            }
            try {
                double price = Double.parseDouble(priceStr.trim());
                int qty = Integer.parseInt(qtyStr.trim());
                manager.add(new Product(code.trim(), name.trim(), price, qty));
                System.out.println("  -> Đã thêm.");
                added++;
            } catch (NumberFormatException e) {
                System.out.println("  -> Từ chối: đơn giá / số lượng phải là số.");
            } catch (IllegalArgumentException e) {
                System.out.println("  -> Từ chối: " + e.getMessage());
            }
        }
        System.out.println("Đã thêm " + added + " sản phẩm hợp lệ.");
    }

    // (Chức năng 5)
    private void saveCsv() {
        try {
            manager.saveToCsv(CSV_FILE);
            System.out.println("Đã lưu " + manager.size() + " sản phẩm vào " + CSV_FILE);
        } catch (IOException e) {
            System.out.println("Lỗi ghi tệp " + CSV_FILE + ": " + e.getMessage());
        }
    }

    // (Chức năng 6 & 10)
    private void loadCsv() {
        try {
            int errors = manager.loadFromCsv(CSV_FILE);
            System.out.println("Đã đọc lại " + manager.size() + " sản phẩm từ " + CSV_FILE
                    + (errors > 0 ? " (bỏ qua " + errors + " dòng lỗi)" : ""));
        } catch (NoSuchFileException e) {
            System.out.println("Không tìm thấy tệp: " + CSV_FILE);
        } catch (IOException e) {
            System.out.println("Lỗi đọc tệp " + CSV_FILE + ": " + e.getMessage());
        }
    }

    // (Chức năng 7)
    private void showAllAndTotal() {
        List<Product> all = manager.getAll();
        if (all.isEmpty()) {
            System.out.println("(Danh sách trống)");
            return;
        }
        for (Product p : all) {
            System.out.println("  " + p);
        }
        System.out.printf("Tổng giá trị tồn kho: %,.0f VND%n", manager.totalInventoryValue());
    }

    // (Chức năng 8)
    private void showMax() {
        manager.maxInventoryValue().ifPresentOrElse(
            p -> System.out.println("Giá trị tồn kho cao nhất: " + p),
            () -> System.out.println("(Danh sách trống)"));
    }

    // (Chức năng 9)
    private void writeReport() {
        try {
            manager.writeReport(REPORT_FILE);
            System.out.println("Đã ghi báo cáo vào " + REPORT_FILE);
        } catch (IOException e) {
            System.out.println("Lỗi ghi tệp " + REPORT_FILE + ": " + e.getMessage());
        }
    }

    /** Đọc một dòng từ bàn phím; trả về null nếu gặp EOF. */
    private String readLine(String prompt) {
        System.out.print(prompt);
        try {
            return keyboard.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException("Lỗi đọc bàn phím", e);
        }
    }
}
