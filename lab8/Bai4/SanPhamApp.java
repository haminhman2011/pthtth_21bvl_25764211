package Bai4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * BAI TAP 4 - Dung PreparedStatement de tang cuong BAO MAT va HIEU NANG.
 *
 *  - CRUD SanPham hoan toan bang PreparedStatement (tham so ?).
 *  - Demo 7: SQL Injection -> Statement bi lua, PreparedStatement thi khong.
 *  - Demo 8: Hieu nang  -> chen N dong bang Statement vs PreparedStatement + batch.
 */
public class SanPhamApp {

    private static final String URL = "jdbc:sqlite:Bai4/sanpham.db";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // ===================== KHOI TAO =====================
    private static void khoiTao() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS SanPham ("
                    + " maSP    TEXT PRIMARY KEY,"
                    + " tenSP   TEXT NOT NULL,"
                    + " gia     REAL NOT NULL CHECK (gia >= 0),"
                    + " soluong INTEGER NOT NULL CHECK (soluong >= 0)"
                    + ")");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM SanPham")) {
                if (rs.next() && rs.getInt(1) > 0) return;
            }
        }
        // Them 4 san pham mau bang PreparedStatement + batch trong 1 giao dich
        Object[][] mau = {
            {"SP01", "Laptop Dell XPS", 25000000.0, 10},
            {"SP02", "Chuot Logitech", 450000.0, 50},
            {"SP03", "Ban phim co", 1200000.0, 30},
            {"SP04", "Man hinh LG 24 inch", 3500000.0, 15},
        };
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO SanPham VALUES (?, ?, ?, ?)")) {
            conn.setAutoCommit(false);
            try {
                for (Object[] sp : mau) {
                    ps.setString(1, (String) sp[0]);
                    ps.setString(2, (String) sp[1]);
                    ps.setDouble(3, (Double) sp[2]);
                    ps.setInt(4, (Integer) sp[3]);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                System.out.println("Da them " + mau.length + " san pham mau (PreparedStatement + batch).");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ===================== READ =====================
    private static int inBang(ResultSet rs) throws SQLException {
        System.out.println("+-------+---------------------------+----------------+----------+");
        System.out.printf("| %-5s | %-25s | %14s | %8s |%n", "MaSP", "Ten san pham", "Gia", "So luong");
        System.out.println("+-------+---------------------------+----------------+----------+");
        int dem = 0;
        while (rs.next()) {
            System.out.printf("| %-5s | %-25s | %,14.0f | %8d |%n",
                    rs.getString("maSP"), rs.getString("tenSP"),
                    rs.getDouble("gia"), rs.getInt("soluong"));
            dem++;
        }
        System.out.println("+-------+---------------------------+----------------+----------+");
        System.out.println("Tong: " + dem + " san pham");
        return dem;
    }

    private static void lietKe() throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM SanPham ORDER BY maSP");
             ResultSet rs = ps.executeQuery()) {
            inBang(rs);
        }
    }

    private static void timTheoTen(String tuKhoa) throws SQLException {
        String sql = "SELECT * FROM SanPham WHERE tenSP LIKE ? ORDER BY maSP";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + tuKhoa + "%");
            try (ResultSet rs = ps.executeQuery()) {
                inBang(rs);
            }
        }
    }

    // ===================== CREATE =====================
    private static void them(String ma, String ten, double gia, int soLuong) throws SQLException {
        String sql = "INSERT INTO SanPham (maSP, tenSP, gia, soluong) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ma);
            ps.setString(2, ten);      // ten co dau ' (vd: "Chuot O'Neil") van khong loi
            ps.setDouble(3, gia);
            ps.setInt(4, soLuong);
            System.out.println("Da them " + ps.executeUpdate() + " san pham.");
        }
    }

    // ===================== UPDATE =====================
    private static void capNhatGia(String ma, double giaMoi) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE SanPham SET gia = ? WHERE maSP = ?")) {
            ps.setDouble(1, giaMoi);
            ps.setString(2, ma);
            int n = ps.executeUpdate();
            System.out.println(n > 0 ? "Da cap nhat gia cho " + ma : "Khong tim thay san pham " + ma);
        }
    }

    private static void capNhatSoLuong(String ma, int soLuongMoi) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE SanPham SET soluong = ? WHERE maSP = ?")) {
            ps.setInt(1, soLuongMoi);
            ps.setString(2, ma);
            int n = ps.executeUpdate();
            System.out.println(n > 0 ? "Da cap nhat so luong cho " + ma : "Khong tim thay san pham " + ma);
        }
    }

    // ===================== DELETE =====================
    private static void xoa(String ma) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM SanPham WHERE maSP = ?")) {
            ps.setString(1, ma);
            int n = ps.executeUpdate();
            System.out.println(n > 0 ? "Da xoa san pham " + ma : "Khong tim thay san pham " + ma);
        }
    }

    // ===================== DEMO 7: SQL INJECTION =====================
    private static void demoSqlInjection(String maNhap) throws SQLException {
        if (maNhap.isEmpty()) maNhap = "SP99' OR '1'='1";
        System.out.println("Gia tri nguoi dung nhap vao o 'Ma SP': " + maNhap);

        // (a) Statement: noi chuoi -> dau ' trong du lieu bien thanh CU PHAP SQL
        String sqlNoiChuoi = "SELECT * FROM SanPham WHERE maSP = '" + maNhap + "'";
        System.out.println("\n(a) Statement - cau SQL that su gui di:");
        System.out.println("    " + sqlNoiChuoi);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlNoiChuoi)) {
            int n = inBang(rs);
            if (maNhap.contains("'") && n > 1)
                System.out.println("    => BI TAN CONG: dieu kien OR '1'='1' luon dung -> lay ra TOAN BO bang!");
        } catch (SQLException e) {
            // dau ' le lam hong cu phap SQL -> cung la dau hieu bi chen ma
            System.out.println("    => Cau SQL bi HONG cu phap boi dau ' trong du lieu: " + e.getMessage());
        }

        // (b) PreparedStatement: gia tri chi la DU LIEU, khong bao gio la cu phap
        System.out.println("\n(b) PreparedStatement - SELECT * FROM SanPham WHERE maSP = ?");
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM SanPham WHERE maSP = ?")) {
            ps.setString(1, maNhap);
            try (ResultSet rs = ps.executeQuery()) {
                inBang(rs);
            }
        }
        System.out.println("    => AN TOAN: CSDL chi tim dung ma \"" + maNhap + "\", khong thuc thi phan chen vao.");
    }

    // ===================== DEMO 8: HIEU NANG =====================
    private static void demoHieuNang(int n) throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS TestHieuNang");
            stmt.executeUpdate("CREATE TABLE TestHieuNang (id INTEGER PRIMARY KEY, ten TEXT, gia REAL)");
            conn.setAutoCommit(false); // ca 2 cach deu chay trong 1 giao dich cho cong bang

            // (a) Statement: moi lan la 1 cau SQL moi -> CSDL phai phan tich (parse) lai N lan
            long t1 = System.nanoTime();
            for (int i = 1; i <= n; i++) {
                stmt.executeUpdate("INSERT INTO TestHieuNang VALUES (" + i + ", 'San pham " + i + "', " + (i * 1000) + ")");
            }
            conn.commit();
            long msStatement = (System.nanoTime() - t1) / 1_000_000;

            stmt.executeUpdate("DELETE FROM TestHieuNang");
            conn.commit();

            // (b) PreparedStatement: bien dich 1 lan, chi thay tham so + gom batch
            long t2 = System.nanoTime();
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO TestHieuNang VALUES (?, ?, ?)")) {
                for (int i = 1; i <= n; i++) {
                    ps.setInt(1, i);
                    ps.setString(2, "San pham " + i);
                    ps.setDouble(3, i * 1000);
                    ps.addBatch();
                    if (i % 1000 == 0) ps.executeBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            long msPrepared = (System.nanoTime() - t2) / 1_000_000;

            stmt.executeUpdate("DROP TABLE TestHieuNang");
            conn.commit();
            conn.setAutoCommit(true);

            System.out.printf("%nChen %,d dong vao bang tam TestHieuNang (moi cach chay trong 1 giao dich):%n", n);
            System.out.println();
            System.out.println("(a) Statement - noi chuoi, moi dong la 1 cau SQL MOI:");
            System.out.println("    INSERT INTO TestHieuNang VALUES (1, 'San pham 1', 1000)");
            System.out.println("    INSERT INTO TestHieuNang VALUES (2, 'San pham 2', 2000)");
            System.out.println("    ...");
            System.out.printf("    -> CSDL phai phan tich (parse) cau SQL %,d lan%n", n);
            System.out.printf("    Thoi gian: %,d ms%n", msStatement);
            System.out.println();
            System.out.println("(b) PreparedStatement + addBatch() - 1 cau SQL co tham so:");
            System.out.println("    INSERT INTO TestHieuNang VALUES (?, ?, ?)");
            System.out.println("    -> bien dich 1 lan, moi dong chi setInt/setString/setDouble roi addBatch()");
            System.out.println("    -> cu 1000 dong goi executeBatch() 1 lan");
            System.out.printf("    Thoi gian: %,d ms%n", msPrepared);
            System.out.println();
            if (msPrepared > 0) {
                System.out.printf("=> PreparedStatement nhanh hon khoang %.1f lan.%n",
                        (double) msStatement / msPrepared);
            } else {
                System.out.println("=> PreparedStatement nhanh hon (chua toi 1 ms).");
            }
            System.out.println("   (Bang tam TestHieuNang da duoc xoa, khong anh huong bang SanPham.)");
        }
    }

    // ===================== MENU =====================
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            khoiTao();
        } catch (ClassNotFoundException e) {
            System.err.println("Khong tim thay driver SQLite! Kiem tra -cp co lib/sqlite-jdbc-*.jar chua.");
            return;
        } catch (SQLException e) {
            System.err.println("Loi khoi tao CSDL: " + e.getMessage());
            return;
        }

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println();
            System.out.println("===== QUAN LY SAN PHAM (PreparedStatement) =====");
            System.out.println("1. Liet ke tat ca san pham");
            System.out.println("2. Tim san pham theo ten");
            System.out.println("3. Them san pham");
            System.out.println("4. Cap nhat gia");
            System.out.println("5. Cap nhat so luong");
            System.out.println("6. Xoa san pham");
            System.out.println("7. Demo SQL Injection: Statement vs PreparedStatement");
            System.out.println("8. Demo hieu nang: Statement vs PreparedStatement + batch");
            System.out.println("0. Thoat");
            System.out.print("Chon: ");
            if (!sc.hasNextLine()) break;
            String chon = sc.nextLine().trim();

            try {
                switch (chon) {
                    case "1" -> lietKe();
                    case "2" -> timTheoTen(nhap(sc, "Nhap tu khoa ten: "));
                    case "3" -> them(nhap(sc, "Ma SP: "), nhap(sc, "Ten SP: "),
                            Double.parseDouble(nhap(sc, "Gia: ")),
                            Integer.parseInt(nhap(sc, "So luong: ")));
                    case "4" -> capNhatGia(nhap(sc, "Ma SP can doi gia: "),
                            Double.parseDouble(nhap(sc, "Gia moi: ")));
                    case "5" -> capNhatSoLuong(nhap(sc, "Ma SP can doi so luong: "),
                            Integer.parseInt(nhap(sc, "So luong moi: ")));
                    case "6" -> xoa(nhap(sc, "Ma SP can xoa: "));
                    case "7" -> demoSqlInjection(nhap(sc,
                            "Nhap ma SP (Enter de dung chuoi tan cong mau  SP99' OR '1'='1 ): "));
                    case "8" -> {
                        String s = nhap(sc, "So dong can chen (Enter = 5000): ");
                        demoHieuNang(s.isEmpty() ? 5000 : Integer.parseInt(s));
                    }
                    case "0" -> {
                        System.out.println("Tam biet!");
                        return;
                    }
                    default -> System.out.println("Lua chon khong hop le!");
                }
            } catch (SQLException e) {
                System.err.println("Loi SQL: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.err.println("Gia / so luong phai la so!");
            }
        }
    }

    private static String nhap(Scanner sc, String thongBao) {
        System.out.print(thongBao);
        return sc.hasNextLine() ? sc.nextLine().trim() : "";
    }
}
