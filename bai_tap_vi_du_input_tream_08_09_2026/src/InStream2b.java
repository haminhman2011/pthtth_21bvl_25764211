/**
 * author: manha.vn
 */
import static java.lang.Thread.*;   // để gọi sleep() trực tiếp

import java.io.IOException;
import java.io.InputStream;

public class InStream2b {
    public static void main(String[] args) throws InterruptedException, IOException {
        InputStream is = System.in;

        try {
            while (true) {
                // Kiểm tra xem có bao nhiêu byte đang sẵn sàng mà không bị chặn
                if (is.available() > 0) {
                    byte[] buffer = new byte[is.available()];
                    int bytesRead = is.read(buffer);
                    if (bytesRead == -1) {
                        break;                       // Kết thúc input stream
                    }
                    String str = new String(buffer, 0, bytesRead);
                    System.out.print(str);           // In ra dữ liệu đọc được
                } else {
                    // Không có dữ liệu sẵn: in dấu '.' và nghỉ một chút
                    System.out.print('.');
                    sleep(100);                      // Giảm tải CPU
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}