package Bai5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * BAI TAP 5 - Truy van nang cao voi ham tong hop (Aggregation Functions).
 * Bang NhanVien(id, ten, chuc_vu, luong)
 *  - SUM(luong)   : tong luong
 *  - AVG(luong)   : luong trung binh
 *  - MAX / MIN    : nhan vien luong cao nhat / thap nhat
 *  - COUNT + GROUP BY chuc_vu : dem nhan vien theo chuc vu
 */
public class ThongKeLuong {

    private static final String URL = "jdbc:sqlite:Bai5/nhanvien.db";

    public static void main(String[] args) {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(URL);

            taoDuLieu(conn);
            inDanhSach(conn);
            tongHop(conn);
            caoNhatThapNhat(conn);
            demTheoChucVu(conn);
            chucVuLuongTBTren(conn, 15_000_000);

        } catch (ClassNotFoundException e) {
            System.err.println("Khong tim thay driver SQLite! Kiem tra -cp co lib/sqlite-jdbc-*.jar chua.");
        } catch (SQLException e) {
            System.err.println("Loi SQL: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Loi dong ket noi: " + e.getMessage());
            }
        }
    }

    // ---------- Tao bang va them nhieu ban ghi co cot luong ----------
    private static void taoDuLieu(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS NhanVien");
            stmt.executeUpdate("CREATE TABLE NhanVien ("
                    + " id      INTEGER PRIMARY KEY,"
                    + " ten     TEXT NOT NULL,"
                    + " chuc_vu TEXT,"
                    + " luong   REAL NOT NULL"
                    + ")");
        }

        Object[][] ds = {
            {"Nguyen Van An",   "Giam doc",        45_000_000},
            {"Tran Thi Binh",   "Ke toan",         14_000_000},
            {"Le Van Cuong",    "Lap trinh vien",  22_000_000},
            {"Pham Thi Dung",   "Lap trinh vien",  18_500_000},
            {"Hoang Van Em",    "Lap trinh vien",  27_000_000},
            {"Vo Thi Giang",    "Tester",          13_000_000},
            {"Dang Van Hung",   "Tester",          12_500_000},
            {"Bui Thi Lan",     "Ke toan",         15_500_000},
            {"Do Van Minh",     "Truong phong",    32_000_000},
            {"Ngo Thi Nga",     "Nhan su",         11_000_000},
            {"Ly Van Phuc",     "Lap trinh vien",  20_000_000},
            {"Mai Thi Quynh",   "Truong phong",    30_000_000},
        };

        String sql = "INSERT INTO NhanVien (ten, chuc_vu, luong) VALUES (?, ?, ?)";
        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] nv : ds) {
                ps.setString(1, (String) nv[0]);
                ps.setString(2, (String) nv[1]);
                ps.setDouble(3, (Integer) nv[2]);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            System.out.println("Da them " + ds.length + " nhan vien (1 giao dich).");
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static void inDanhSach(Connection conn) throws SQLException {
        tieuDe("DANH SACH NHAN VIEN");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM NhanVien ORDER BY id")) {
            System.out.printf("%-3s | %-16s | %-15s | %14s%n", "ID", "Ten", "Chuc vu", "Luong");
            System.out.println("----+------------------+-----------------+---------------");
            while (rs.next()) {
                System.out.printf("%-3d | %-16s | %-15s | %,14.0f%n", rs.getInt("id"),
                        rs.getString("ten"), rs.getString("chuc_vu"), rs.getDouble("luong"));
            }
        }
    }

    // ---------- SUM, AVG, COUNT ----------
    private static void tongHop(Connection conn) throws SQLException {
        tieuDe("1. TONG LUONG - SUM(luong)  &  2. LUONG TRUNG BINH - AVG(luong)");
        String sql = "SELECT COUNT(*) AS so_nv, SUM(luong) AS tong, AVG(luong) AS tb FROM NhanVien";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                System.out.println("SQL: " + sql);
                System.out.printf("So nhan vien      : %d%n", rs.getInt("so_nv"));
                System.out.printf("Tong luong        : %,.0f VND%n", rs.getDouble("tong"));
                System.out.printf("Luong trung binh  : %,.0f VND%n", rs.getDouble("tb"));
            }
        }
    }

    // ---------- MAX / MIN: lay ca thong tin nhan vien bang truy van con ----------
    private static void caoNhatThapNhat(Connection conn) throws SQLException {
        tieuDe("3. NHAN VIEN LUONG CAO NHAT / THAP NHAT - MAX / MIN(luong)");
        inNhanVienTheo(conn, "MAX", "Cao nhat");
        inNhanVienTheo(conn, "MIN", "Thap nhat");
    }

    private static void inNhanVienTheo(Connection conn, String ham, String nhan) throws SQLException {
        // Dung truy van con de neu co nhieu nguoi cung muc luong thi lay ra het
        String sql = "SELECT id, ten, chuc_vu, luong FROM NhanVien"
                + " WHERE luong = (SELECT " + ham + "(luong) FROM NhanVien)";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("SQL: " + sql);
            while (rs.next()) {
                System.out.printf("  %-9s: #%d %s (%s) - %,.0f VND%n", nhan, rs.getInt("id"),
                        rs.getString("ten"), rs.getString("chuc_vu"), rs.getDouble("luong"));
            }
        }
    }

    // ---------- COUNT + GROUP BY ----------
    private static void demTheoChucVu(Connection conn) throws SQLException {
        tieuDe("4. SO LUONG NHAN VIEN THEO CHUC VU - COUNT + GROUP BY");
        String sql = "SELECT chuc_vu, COUNT(*) AS so_luong, SUM(luong) AS tong, AVG(luong) AS tb"
                + " FROM NhanVien GROUP BY chuc_vu ORDER BY so_luong DESC, chuc_vu";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("SQL: " + sql);
            System.out.printf("%-15s | %8s | %14s | %14s%n", "Chuc vu", "So luong", "Tong luong", "Luong TB");
            System.out.println("----------------+----------+----------------+---------------");
            while (rs.next()) {
                System.out.printf("%-15s | %8d | %,14.0f | %,14.0f%n", rs.getString("chuc_vu"),
                        rs.getInt("so_luong"), rs.getDouble("tong"), rs.getDouble("tb"));
            }
        }
    }

    // ---------- (Mo rong) GROUP BY + HAVING voi PreparedStatement ----------
    private static void chucVuLuongTBTren(Connection conn, double nguong) throws SQLException {
        tieuDe(String.format("5. (MO RONG) CHUC VU CO LUONG TB > %,.0f - GROUP BY + HAVING", nguong));
        String sql = "SELECT chuc_vu, COUNT(*) AS so_luong, AVG(luong) AS tb FROM NhanVien"
                + " GROUP BY chuc_vu HAVING AVG(luong) > ? ORDER BY tb DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, nguong);
            System.out.println("SQL: " + sql);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("  %-15s : %d nguoi, TB %,.0f VND%n", rs.getString("chuc_vu"),
                            rs.getInt("so_luong"), rs.getDouble("tb"));
                }
            }
        }
    }

    private static void tieuDe(String s) {
        System.out.println();
        System.out.println("===== " + s + " =====");
    }
}
