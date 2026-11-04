package Bai1;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * BÀI 1 - Chương trình SERVER
 *
 * Các bước:
 *  B1. Tạo đối tượng từ xa (HelloImpl)
 *  B2. Tạo RMI Registry tại cổng 1099 (Registry giống như "danh bạ" chứa tên các dịch vụ)
 *  B3. Đăng ký đối tượng vào Registry với 1 cái tên, ở đây là "HelloService"
 *  => Sau đó server cứ chạy và chờ client gọi tới.
 */
public class HelloServer {

    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "HelloService";

    // Để registry là biến static để không bị Java dọn rác (garbage collect) mất
    private static Registry registry;

    public static void main(String[] args) {
        try {
            // B1: tạo đối tượng từ xa
            HelloImpl hello = new HelloImpl();

            // B2: tạo registry ngay trong chương trình (không cần chạy lệnh rmiregistry riêng)
            registry = LocateRegistry.createRegistry(PORT);

            // B3: đăng ký đối tượng với tên SERVICE_NAME
            // rebind = nếu tên đã tồn tại thì ghi đè, không bị lỗi
            registry.rebind(SERVICE_NAME, hello);

            System.out.println("===== HELLO SERVER =====");
            System.out.println("Server da san sang tai cong " + PORT);
            System.out.println("Ten dich vu: " + SERVICE_NAME);
            System.out.println("Dang cho client ket noi... (Ctrl + C de tat server)");
        } catch (Exception e) {
            System.out.println("Loi server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
