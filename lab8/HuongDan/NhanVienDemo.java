package HuongDan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * HUONG DAN THUC HANH JDBC (B1 -> B5) voi bang NhanVien.
 * - B1: nap driver + ket noi
 * - B2: tao Statement
 * - B3: tao bang + them du lieu (trong 1 giao dich)
 * - B4: SELECT va duyet ResultSet
 * - B5: dong tai nguyen theo thu tu nguoc lai (trong finally)
 */
public class NhanVienDemo {

    // File CSDL nam trong thu muc HuongDan (chay lenh tu thu muc lab8)
    private static final String URL = "jdbc:sqlite:HuongDan/nhanvien.db";
    // SQLite khong can user/pass, de trong cho dung mau getConnection(url, user, pass)
    private static final String USERNAME = "";
    private static final String PASSWD = "";

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // ===== B1. Nap driver va thiet lap ket noi =====
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(URL, USERNAME, PASSWD);
            System.out.println("[B1] Ket noi thanh cong: " + URL);

            // ===== B2. Tao Statement tu Connection =====
            stmt = conn.createStatement();
            System.out.println("[B2] Da tao Statement");

            // ===== B3.1. Tao bang =====
            // Xoa bang cu de moi lan chay deu bat dau tu dau
            stmt.executeUpdate("DROP TABLE IF EXISTS NhanVien");
            String sql_create_table = "CREATE TABLE NhanVien ("
                    + " id      INTEGER PRIMARY KEY,"
                    + " ten     TEXT NOT NULL,"
                    + " chuc_vu TEXT"
                    + ")";
            stmt.executeUpdate(sql_create_table);
            System.out.println("[B3.1] Da tao bang NhanVien");

            // ===== B3.2. Them du lieu - dung giao dich (transaction) =====
            conn.setAutoCommit(false); // tat auto-commit
            String[] sql_inserts = {
                "INSERT INTO NhanVien (id, ten, chuc_vu) VALUES (1, 'Nguyen Van An', 'Giam doc')",
                "INSERT INTO NhanVien (id, ten, chuc_vu) VALUES (2, 'Tran Thi Binh', 'Ke toan')",
                "INSERT INTO NhanVien (id, ten, chuc_vu) VALUES (3, 'Le Van Cuong', 'Lap trinh vien')",
                "INSERT INTO NhanVien (id, ten, chuc_vu) VALUES (4, 'Pham Thi Dung', 'Lap trinh vien')"
            };
            for (String sql_insert : sql_inserts) {
                stmt.executeUpdate(sql_insert);
            }
            conn.commit(); // chi commit khi TAT CA lenh INSERT thanh cong
            conn.setAutoCommit(true);
            System.out.println("[B3.2] Da them " + sql_inserts.length + " nhan vien (commit)");

            // ===== B4. Truy van va hien thi du lieu =====
            String sql_select = "SELECT id, ten, chuc_vu FROM NhanVien";
            rs = stmt.executeQuery(sql_select);

            System.out.println("[B4] Danh sach nhan vien:");
            System.out.println("+----+----------------------+------------------+");
            System.out.printf("| %-2s | %-20s | %-16s |%n", "ID", "Ten", "Chuc vu");
            System.out.println("+----+----------------------+------------------+");
            while (rs.next()) {
                int id = rs.getInt("id");
                String ten = rs.getString("ten");
                String chucVu = rs.getString("chuc_vu");
                System.out.printf("| %-2d | %-20s | %-16s |%n", id, ten, chucVu);
            }
            System.out.println("+----+----------------------+------------------+");

        } catch (ClassNotFoundException e) {
            System.err.println("Khong tim thay driver SQLite! Kiem tra lai -cp co file lib/sqlite-jdbc-*.jar chua.");
        } catch (SQLException e) {
            System.err.println("Loi SQL: " + e.getMessage());
            // Neu loi xay ra giua giao dich -> huy bo tat ca thay doi
            try {
                if (conn != null && !conn.getAutoCommit()) {
                    conn.rollback();
                    System.err.println("Da rollback giao dich.");
                }
            } catch (SQLException ex) {
                System.err.println("Loi khi rollback: " + ex.getMessage());
            }
        } finally {
            // ===== B5. Dong tai nguyen theo thu tu nguoc lai: rs -> stmt -> conn =====
            try { if (rs != null) rs.close(); } catch (SQLException e) { System.err.println("Loi dong ResultSet: " + e.getMessage()); }
            try { if (stmt != null) stmt.close(); } catch (SQLException e) { System.err.println("Loi dong Statement: " + e.getMessage()); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { System.err.println("Loi dong Connection: " + e.getMessage()); }
            System.out.println("[B5] Da dong ResultSet, Statement, Connection");
        }
    }
}
