package Bai5;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * BÀI 5 - Chương trình SERVER chat
 *
 * - Đăng ký ChatServerImpl vào registry với tên "ChatService"
 * - Sau đó đọc bàn phím: mỗi dòng gõ vào là 1 tin nhắn phản hồi gửi cho client
 * - Gõ "exit" để tắt server
 */
public class ChatServerMain {

    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "ChatService";

    private static Registry registry;

    public static void main(String[] args) {
        try {
            ChatServerImpl server = new ChatServerImpl();

            registry = LocateRegistry.createRegistry(PORT);
            registry.rebind(SERVICE_NAME, server);

            System.out.println("===== CHAT SERVER =====");
            System.out.println("Server dang chay tai cong " + PORT + ", ten dich vu: " + SERVICE_NAME);
            System.out.println("Dang cho client vao chat... (go 'exit' de tat server)");

            // Luồng chính: đọc tin nhắn người dùng gõ ở server và gửi cho client
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.equalsIgnoreCase("exit")) {
                    break;
                }
                if (!line.trim().isEmpty()) {
                    server.replyToClient(line);
                }
            }

            System.out.println("Tat server.");
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Loi server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
