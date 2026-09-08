
/**
 * Ví dụ 1 — Đọc từng ký tự với InputStream
 */

import java.io.IOException;
import java.io.InputStream;

public class InputStream1 {
    public static void main(String[] args) {
        InputStream is = System.in;
        while (true) {
            try {
                int ch = is.read(); // đọc 1 byte, trả về -1 khi hết
                if (ch == -1 || ch == 'q') { // dừng khi hết dữ liệu hoặc gặp 'q'
                    break;
                }
                System.out.println((char) ch); // ép kiểu int -> char để in ký tự
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }
    }
}