package Bai3;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * BAI TAP 3 - Lam lai Bai tap 1 voi try-catch-finally NGHIEM NGAT.
 *
 * Quy tac ap dung cho MOI ham truy cap CSDL:
 *   1. Khai bao conn / stmt / rs = null BEN NGOAI khoi try.
 *   2. Moi thao tac JDBC nam trong try.
 *   3. catch (SQLException) -> in loi (va rollback neu dang trong giao dich).
 *   4. finally -> LUON goi DbUtil.close(rs, stmt, conn), ke ca khi co loi.
 */
public class SanPhamApp {

    // ===================== KHOI TAO =====================
    private static boolean khoiTao() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS SanPham ("
                    + " maSP    TEXT PRIMARY KEY,"
                    + " tenSP   TEXT NOT NULL,"
                    + " gia     REAL NOT NULL CHECK (gia >= 0),"
                    + " soluong INTEGER NOT NULL CHECK (soluong >= 0)"
                    + ")");

            rs = stmt.executeQuery("SELECT COUNT(*) FROM SanPham");
            if (rs.next() && rs.getInt(1) == 0) {
                // Them du lieu mau trong 1 giao dich
                conn.setAutoCommit(false);
                stmt.executeUpdate("INSERT INTO SanPham VALUES ('SP01', 'Laptop Dell XPS', 25000000, 10)");
                stmt.executeUpdate("INSERT INTO SanPham VALUES ('SP02', 'Chuot Logitech', 450000, 50)");
                stmt.executeUpdate("INSERT INTO SanPham VALUES ('SP03', 'Ban phim co', 1200000, 30)");
                stmt.executeUpdate("INSERT INTO SanPham VALUES ('SP04', 'Man hinh LG 24 inch', 3500000, 15)");
                conn.commit();
                System.out.println("Da them 4 san pham mau vao bang SanPham.");
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Loi khoi tao CSDL: " + e.getMessage());
            DbUtil.rollback(conn);
            return false;
        } finally {
            DbUtil.close(rs, stmt, conn);
        }
    }

    // ===================== READ =====================
    private static void lietKe() {
        truyVanVaIn("SELECT * FROM SanPham ORDER BY maSP");
    }

    private static void timTheoTen(String tuKhoa) {
        truyVanVaIn("SELECT * FROM SanPham WHERE tenSP LIKE '%" + tuKhoa + "%' ORDER BY maSP");
    }

    private static void truyVanVaIn(String sql) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

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
        } catch (SQLException e) {
            System.err.println("Loi truy van: " + e.getMessage());
        } finally {
            DbUtil.close(rs, stmt, conn);
        }
    }

    // ===================== CREATE / UPDATE / DELETE =====================
    /** Thuc thi 1 lenh INSERT/UPDATE/DELETE, tra ve so dong bi anh huong (-1 neu loi). */
    private static int capNhat(String sql) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DbUtil.getConnection();
            stmt = conn.createStatement();
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Loi SQL: " + e.getMessage());
            return -1;
        } finally {
            DbUtil.close(stmt, conn);
        }
    }

    private static void them(String ma, String ten, double gia, int soLuong) {
        int n = capNhat("INSERT INTO SanPham (maSP, tenSP, gia, soluong) VALUES ('"
                + ma + "', '" + ten + "', " + gia + ", " + soLuong + ")");
        if (n > 0) System.out.println("Da them san pham " + ma);
    }

    private static void capNhatGia(String ma, double giaMoi) {
        int n = capNhat("UPDATE SanPham SET gia = " + giaMoi + " WHERE maSP = '" + ma + "'");
        thongBao(n, "Da cap nhat gia cho " + ma, ma);
    }

    private static void capNhatSoLuong(String ma, int soLuongMoi) {
        int n = capNhat("UPDATE SanPham SET soluong = " + soLuongMoi + " WHERE maSP = '" + ma + "'");
        thongBao(n, "Da cap nhat so luong cho " + ma, ma);
    }

    private static void xoa(String ma) {
        int n = capNhat("DELETE FROM SanPham WHERE maSP = '" + ma + "'");
        thongBao(n, "Da xoa san pham " + ma, ma);
    }

    private static void thongBao(int n, String thanhCong, String ma) {
        if (n > 0) System.out.println(thanhCong);
        else if (n == 0) System.out.println("Khong tim thay san pham " + ma);
        // n == -1: loi da duoc in trong catch
    }

    // ===================== DEMO: LOI VAN DONG TAI NGUYEN =====================
    /**
     * Co tinh truy van 1 bang khong ton tai -> SQLException.
     * Bat log de thay khoi finally van dong Statement va Connection.
     */
    private static void demoLoi() {
        DbUtil.LOG_CLOSE = true;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            stmt = conn.createStatement();
            System.out.println("Thuc thi: SELECT * FROM BangKhongTonTai");
            rs = stmt.executeQuery("SELECT * FROM BangKhongTonTai"); // nem SQLException
            System.out.println("(dong nay se khong duoc in)");
        } catch (SQLException e) {
            System.err.println("[catch] Bat duoc loi: " + e.getMessage());
        } finally {
            DbUtil.close(rs, stmt, conn);
            try {
                System.out.println("   Kiem tra: stmt.isClosed() = " + stmt.isClosed()
                        + ", conn.isClosed() = " + conn.isClosed());
            } catch (SQLException ignored) {
            }
            DbUtil.LOG_CLOSE = false;
        }
    }

    // ===================== MENU =====================
    public static void main(String[] args) {
        if (!khoiTao()) return;

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println();
            System.out.println("===== QUAN LY SAN PHAM (try-catch-finally) =====");
            System.out.println("1. Liet ke tat ca san pham");
            System.out.println("2. Tim san pham theo ten");
            System.out.println("3. Them san pham");
            System.out.println("4. Cap nhat gia");
            System.out.println("5. Cap nhat so luong");
            System.out.println("6. Xoa san pham");
            System.out.println("7. Demo: gay loi SQL va kiem tra finally van dong tai nguyen");
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
                    case "7" -> demoLoi();
                    case "0" -> {
                        System.out.println("Tam biet!");
                        return;
                    }
                    default -> System.out.println("Lua chon khong hop le!");
                }
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
