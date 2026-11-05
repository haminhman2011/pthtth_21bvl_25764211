package Bai2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BAI TAP 2 - Su dung giao dich (Transaction) trong cap nhat du lieu.
 *
 * Tinh huong "ban hang": 1 don hang gom nhieu san pham. Voi MOI san pham phai
 *   (1) tru so luong ton kho trong SanPham
 *   (2) ghi 1 dong vao ChiTietDonHang
 * Tat ca cac lenh nay nam trong 1 giao dich: hoac thanh cong het (commit),
 * hoac co 1 loi la huy het (rollback) -> du lieu khong bao gio bi "lech".
 */
public class GiaoDichDemo {

    private static final String URL = "jdbc:sqlite:Bai2/banhang.db";

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Khong tim thay driver SQLite! Kiem tra -cp co lib/sqlite-jdbc-*.jar chua.");
            return;
        }

        try {
            khoiTao();
            inKho("TRANG THAI BAN DAU");

            // ---- Tinh huong 1: don hang hop le -> COMMIT ----
            Map<String, Integer> don1 = new LinkedHashMap<>();
            don1.put("SP01", 2);
            don1.put("SP02", 5);
            banHang("DH001", don1);
            inKho("SAU DON DH001");

            // ---- Tinh huong 2: SP03 khong du hang -> ROLLBACK ca SP01 da tru truoc do ----
            Map<String, Integer> don2 = new LinkedHashMap<>();
            don2.put("SP01", 1);
            don2.put("SP03", 999);
            banHang("DH002", don2);
            inKho("SAU DON DH002 (phai giong het buoc truoc)");

            // ---- Tinh huong 3: tang gia + nhap kho, nhung lenh cuoi bi loi SQL -> ROLLBACK ----
            capNhatHangLoat();
            inKho("SAU KHI CAP NHAT HANG LOAT THAT BAI (phai giong het buoc truoc)");

            inDonHang();
        } catch (SQLException e) {
            System.err.println("Loi SQL: " + e.getMessage());
        }
    }

    // ================== GIAO DICH 1: BAN HANG ==================
    private static void banHang(String maDon, Map<String, Integer> gioHang) {
        System.out.println("\n>>> Tao don hang " + maDon + ": " + gioHang);
        Connection conn = null;
        PreparedStatement psKiemTra = null, psTruKho = null, psChiTiet = null;
        try {
            conn = DriverManager.getConnection(URL);
            conn.setAutoCommit(false); // BAT DAU giao dich

            psKiemTra = conn.prepareStatement("SELECT tenSP, soluong FROM SanPham WHERE maSP = ?");
            psTruKho = conn.prepareStatement("UPDATE SanPham SET soluong = soluong - ? WHERE maSP = ?");
            psChiTiet = conn.prepareStatement(
                    "INSERT INTO ChiTietDonHang (maDon, maSP, soluong) VALUES (?, ?, ?)");

            for (Map.Entry<String, Integer> item : gioHang.entrySet()) {
                String maSP = item.getKey();
                int soLuongMua = item.getValue();

                // Kiem tra ton kho
                psKiemTra.setString(1, maSP);
                try (ResultSet rs = psKiemTra.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Khong ton tai san pham " + maSP);
                    }
                    int ton = rs.getInt("soluong");
                    if (ton < soLuongMua) {
                        throw new SQLException("San pham " + maSP + " (" + rs.getString("tenSP")
                                + ") chi con " + ton + ", khong du " + soLuongMua);
                    }
                }

                // Tru kho
                psTruKho.setInt(1, soLuongMua);
                psTruKho.setString(2, maSP);
                psTruKho.executeUpdate();

                // Ghi chi tiet don hang
                psChiTiet.setString(1, maDon);
                psChiTiet.setString(2, maSP);
                psChiTiet.setInt(3, soLuongMua);
                psChiTiet.executeUpdate();

                System.out.println("    - Da tru kho " + maSP + " x" + soLuongMua + " (chua commit)");
            }

            conn.commit(); // tat ca deu OK -> luu vinh vien
            System.out.println("    => COMMIT: don hang " + maDon + " thanh cong.");

        } catch (SQLException e) {
            System.out.println("    !! Loi: " + e.getMessage());
            rollback(conn);
        } finally {
            dong(psChiTiet);
            dong(psTruKho);
            dong(psKiemTra);
            dongKetNoi(conn);
        }
    }

    // ================== GIAO DICH 2: CAP NHAT HANG LOAT ==================
    private static void capNhatHangLoat() {
        System.out.println("\n>>> Cap nhat hang loat: tang gia 10% + nhap them 100 SP02 + them SP01 (trung ma)");
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(URL);
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            int n1 = stmt.executeUpdate("UPDATE SanPham SET gia = gia * 1.1");
            System.out.println("    - Tang gia " + n1 + " san pham (chua commit)");

            int n2 = stmt.executeUpdate("UPDATE SanPham SET soluong = soluong + 100 WHERE maSP = 'SP02'");
            System.out.println("    - Nhap kho SP02: " + n2 + " dong (chua commit)");

            // Lenh nay vi pham PRIMARY KEY -> SQLException
            stmt.executeUpdate("INSERT INTO SanPham VALUES ('SP01', 'San pham trung ma', 1000, 1)");

            conn.commit();
            System.out.println("    => COMMIT");
        } catch (SQLException e) {
            System.out.println("    !! Loi: " + e.getMessage());
            rollback(conn);
        } finally {
            dong(stmt);
            dongKetNoi(conn);
        }
    }

    // ================== HAM HO TRO ==================
    private static void rollback(Connection conn) {
        if (conn == null) return;
        try {
            conn.rollback();
            System.out.println("    => ROLLBACK: huy bo toan bo thay doi trong giao dich.");
        } catch (SQLException ex) {
            System.err.println("Loi khi rollback: " + ex.getMessage());
        }
    }

    private static void dong(AutoCloseable r) {
        try {
            if (r != null) r.close();
        } catch (Exception e) {
            System.err.println("Loi khi dong tai nguyen: " + e.getMessage());
        }
    }

    private static void dongKetNoi(Connection conn) {
        if (conn == null) return;
        try {
            conn.setAutoCommit(true); // tra ve trang thai mac dinh
            conn.close();
        } catch (SQLException e) {
            System.err.println("Loi khi dong ket noi: " + e.getMessage());
        }
    }

    private static void khoiTao() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS ChiTietDonHang");
            stmt.executeUpdate("DROP TABLE IF EXISTS SanPham");
            stmt.executeUpdate("CREATE TABLE SanPham ("
                    + " maSP TEXT PRIMARY KEY, tenSP TEXT NOT NULL,"
                    + " gia REAL NOT NULL, soluong INTEGER NOT NULL CHECK (soluong >= 0))");
            stmt.executeUpdate("CREATE TABLE ChiTietDonHang ("
                    + " id INTEGER PRIMARY KEY AUTOINCREMENT, maDon TEXT NOT NULL,"
                    + " maSP TEXT NOT NULL REFERENCES SanPham(maSP), soluong INTEGER NOT NULL)");
            stmt.executeUpdate("INSERT INTO SanPham VALUES ('SP01', 'Laptop Dell XPS', 25000000, 10)");
            stmt.executeUpdate("INSERT INTO SanPham VALUES ('SP02', 'Chuot Logitech', 450000, 50)");
            stmt.executeUpdate("INSERT INTO SanPham VALUES ('SP03', 'Ban phim co', 1200000, 30)");
        }
    }

    private static void inKho(String tieuDe) throws SQLException {
        System.out.println("\n--- " + tieuDe + " ---");
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM SanPham ORDER BY maSP")) {
            System.out.printf("%-5s | %-18s | %14s | %8s%n", "MaSP", "Ten SP", "Gia", "So luong");
            while (rs.next()) {
                System.out.printf("%-5s | %-18s | %,14.0f | %8d%n", rs.getString("maSP"),
                        rs.getString("tenSP"), rs.getDouble("gia"), rs.getInt("soluong"));
            }
        }
    }

    private static void inDonHang() throws SQLException {
        System.out.println("\n--- BANG ChiTietDonHang (chi co don DH001 vi DH002 da rollback) ---");
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM ChiTietDonHang ORDER BY id")) {
            while (rs.next()) {
                System.out.printf("#%d  %s  %s  x%d%n", rs.getInt("id"), rs.getString("maDon"),
                        rs.getString("maSP"), rs.getInt("soluong"));
            }
        }
    }
}
