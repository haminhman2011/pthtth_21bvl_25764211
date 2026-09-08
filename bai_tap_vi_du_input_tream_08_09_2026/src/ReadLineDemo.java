/**
 * author: manha.vn
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadLineDemo {
    public static void main(String[] args) {
        InputStream is = System.in;
        InputStreamReader isr = new InputStreamReader(is);   // byte -> ký tự
        BufferedReader br = new BufferedReader(isr);          // gom thành dòng
        while (true) {
            try {
                String line = br.readLine();     // đọc trọn 1 dòng, null khi hết
                if (line == null) break;
                System.out.println(line);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}