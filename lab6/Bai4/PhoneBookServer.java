package Bai4;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * BÀI 4 - Chương trình SERVER quản lý danh bạ
 */
public class PhoneBookServer {

    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "PhoneBookService";

    private static Registry registry;

    public static void main(String[] args) {
        try {
            PhoneBookImpl phoneBook = new PhoneBookImpl();

            registry = LocateRegistry.createRegistry(PORT);
            registry.rebind(SERVICE_NAME, phoneBook);

            System.out.println("===== PHONE BOOK SERVER =====");
            System.out.println("Server dang chay tai cong " + PORT + ", ten dich vu: " + SERVICE_NAME);
            System.out.println("Dang cho client... (Ctrl + C de tat)");
        } catch (Exception e) {
            System.out.println("Loi server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
