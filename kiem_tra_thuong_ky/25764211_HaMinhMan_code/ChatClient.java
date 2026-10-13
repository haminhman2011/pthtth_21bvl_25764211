import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * CLIENT CHAT NHÓM QUA TCP
 * - Kết nối tới server (localhost:5000).
 * - 1 luồng phụ: liên tục nhận tin từ server và in ra màn hình.
 * - Luồng chính: đọc bàn phím và gửi lên server.
 * - Gõ /quit để rời phòng, /exit để ngắt kết nối.
 */
public class ChatClient {

    public static final String HOST = "localhost";
    public static final int PORT = 5000;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(HOST, PORT);
            System.out.println("Da ket noi toi server " + HOST + ":" + PORT);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // Luồng nhận tin nhắn từ server
            Thread receiveThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    // socket bị đóng -> dừng luồng
                }
                System.out.println("Da ngat ket noi voi server.");
            });
            receiveThread.start();

            // Luồng chính: đọc bàn phím và gửi đi
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                String msg = sc.nextLine();
                out.println(msg);

                if (msg.trim().equals("/exit")) {
                    break;
                }
            }

            // Đợi server gửi lời chào tạm biệt rồi đóng kết nối
            Thread.sleep(300);
            socket.close();
            sc.close();
        } catch (IOException e) {
            System.out.println("Khong ket noi duoc server: " + e.getMessage());
        } catch (InterruptedException e) {
            // bỏ qua
        }
    }
}
