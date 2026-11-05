package Bai3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Lop tien ich: mo ket noi va DONG TAI NGUYEN an toan.
 * Moi tai nguyen duoc dong trong 1 khoi try-catch rieng, nen neu dong rs bi loi
 * thi van tiep tuc dong stmt va conn -> khong ro ri tai nguyen.
 */
public class DbUtil {

    public static final String URL = "jdbc:sqlite:Bai3/sanpham.db";

    /** Bat/tat log khi dong tai nguyen (de quan sat finally co chay hay khong). */
    public static boolean LOG_CLOSE = false;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "Khong tim thay driver SQLite! Kiem tra -cp co lib/sqlite-jdbc-*.jar chua.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /** Dong theo thu tu NGUOC LAI: ResultSet -> Statement -> Connection. */
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
                log("ResultSet");
            } catch (SQLException e) {
                System.err.println("Loi khi dong ResultSet: " + e.getMessage());
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
                log("Statement");
            } catch (SQLException e) {
                System.err.println("Loi khi dong Statement: " + e.getMessage());
            }
        }
        if (conn != null) {
            try {
                conn.close();
                log("Connection");
            } catch (SQLException e) {
                System.err.println("Loi khi dong Connection: " + e.getMessage());
            }
        }
    }

    public static void close(Statement stmt, Connection conn) {
        close(null, stmt, conn);
    }

    public static void rollback(Connection conn) {
        if (conn == null) return;
        try {
            if (!conn.getAutoCommit()) {
                conn.rollback();
                System.out.println("=> Da ROLLBACK giao dich.");
            }
        } catch (SQLException e) {
            System.err.println("Loi khi rollback: " + e.getMessage());
        }
    }

    private static void log(String ten) {
        if (LOG_CLOSE) System.out.println("   [finally] da dong " + ten);
    }
}
