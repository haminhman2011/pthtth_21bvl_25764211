package Bai1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * BAI TAP 1 - Quan ly danh muc san pham (CRUD) dung Statement.
 * Bang SanPham(maSP, tenSP, gia, soluong)
 *  - Create : them san pham
 *  - Read   : liet ke tat ca / tim theo tenSP
 *  - Update : doi gia hoac so luong theo maSP
 *  - Delete : xoa theo maSP
 */
public class SanPhamApp {

    private static final String URL = "jdbc:sqlite:Bai1/sanpham.db";

    // ---------- Ket noi ----------
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // ---------- Tao bang + du lieu mau ----------
    private static void khoiTao() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS SanPham ("
                    + " maSP    TEXT PRIMARY KEY,"
                    + " tenSP   TEXT NOT NULL,"
                    + " gia     REAL NOT NULL CHECK (gia >= 0),"
                    + " soluong INTEGER NOT NULL CHECK (soluong >= 0)"
                    + ")");

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM SanPham")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Create: them it nhat 3 san pham
                    stmt.executeUpdate("INSERT INTO SanPham VALUES ('SP01', 'Laptop Dell XPS', 25000000, 10)");
                    stmt.executeUpdate("INSERT INTO SanPham VALUES ('SP02', 'Chuot Logitech', 450000, 50)");
                    stmt.executeUpdate("INSERT INTO SanPham VALUES ('SP03', 'Ban phim co', 1200000, 30)");
                    stmt.executeUpdate("INSERT INTO SanPham VALUES ('SP04', 'Man hinh LG 24 inch', 3500000, 15)");
                    System.out.println("Da them 4 san pham mau vao bang SanPham.");
                }
            }
        }
    }

    // ---------- READ ----------
    private static void inBang(ResultSet rs) throws SQLException {
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
    }

    private static void lietKe() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM SanPham ORDER BY maSP")) {
            inBang(rs);
        }
    }

    private static void timTheoTen(String tuKhoa) throws SQLException {
        String sql = "SELECT * FROM SanPham WHERE tenSP LIKE '%" + tuKhoa + "%' ORDER BY maSP";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            inBang(rs);
        }
    }

    // ---------- CREATE ----------
    private static void them(String ma, String ten, double gia, int soLuong) throws SQLException {
        String sql = "INSERT INTO SanPham (maSP, tenSP, gia, soluong) VALUES ('"
                + ma + "', '" + ten + "', " + gia + ", " + soLuong + ")";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            int n = stmt.executeUpdate(sql);
            System.out.println("Da them " + n + " san pham.");
        }
    }

    // ---------- UPDATE ----------
    private static void capNhatGia(String ma, double giaMoi) throws SQLException {
        String sql = "UPDATE SanPham SET gia = " + giaMoi + " WHERE maSP = '" + ma + "'";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            int n = stmt.executeUpdate(sql);
            System.out.println(n > 0 ? "Da cap nhat gia cho " + ma : "Khong tim thay san pham " + ma);
        }
    }

    private static void capNhatSoLuong(String ma, int soLuongMoi) throws SQLException {
        String sql = "UPDATE SanPham SET soluong = " + soLuongMoi + " WHERE maSP = '" + ma + "'";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            int n = stmt.executeUpdate(sql);
            System.out.println(n > 0 ? "Da cap nhat so luong cho " + ma : "Khong tim thay san pham " + ma);
        }
    }

    // ---------- DELETE ----------
    private static void xoa(String ma) throws SQLException {
        String sql = "DELETE FROM SanPham WHERE maSP = '" + ma + "'";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            int n = stmt.executeUpdate(sql);
            System.out.println(n > 0 ? "Da xoa san pham " + ma : "Khong tim thay san pham " + ma);
        }
    }

    // ---------- MENU ----------
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
            System.out.println("===== QUAN LY SAN PHAM (Statement) =====");
            System.out.println("1. Liet ke tat ca san pham");
            System.out.println("2. Tim san pham theo ten");
            System.out.println("3. Them san pham");
            System.out.println("4. Cap nhat gia");
            System.out.println("5. Cap nhat so luong");
            System.out.println("6. Xoa san pham");
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
