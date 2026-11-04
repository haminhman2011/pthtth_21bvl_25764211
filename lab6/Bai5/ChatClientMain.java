package Bai5;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * BÀI 5 - Chương trình CLIENT chat
 *
 * - Tìm "ChatService" trên server
 * - Tạo đối tượng callback (ChatClientImpl) và đăng ký với server
 * - Mỗi dòng gõ vào sẽ được gửi lên server bằng sendMessage()
 * - Tin nhắn phản hồi của server được in ra nhờ callback
 * - Gõ "bye" để thoát
 */
public class ChatClientMain {

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        Scanner sc = new Scanner(System.in);

        try {
            Registry registry = LocateRegistry.getRegistry(host, ChatServerMain.PORT);
            ChatServer server = (ChatServer) registry.lookup(ChatServerMain.SERVICE_NAME);

            System.out.println("===== CHAT CLIENT =====");
            System.out.print("Nhap ten cua ban: ");
            String name = sc.nextLine().trim();
            if (name.isEmpty()) name = "Client";

            // Tạo callback và đăng ký với server
            ChatClientImpl callback = new ChatClientImpl();
            server.registerClient(name, callback);

            System.out.println("Da vao phong chat. Go tin nhan roi Enter de gui, go 'bye' de thoat.");

            while (sc.hasNextLine()) {
                String msg = sc.nextLine();
                if (msg.equalsIgnoreCase("bye")) {
                    break;
                }
                if (!msg.trim().isEmpty()) {
                    server.sendMessage(msg); // gửi tin nhắn lên server
                }
            }

            server.unregisterClient();
            System.out.println("Da thoat chat. Tam biet!");
            // Thoát hẳn chương trình (vì callback đang được export nên JVM chưa tự tắt)
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Loi client: " + e.getMessage());
            System.out.println("=> Kiem tra lai xem server da chay chua?");
            System.exit(1);
        }
    }
}
