package Bai3;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * BÀI 3 - Chương trình SERVER kiểm tra số nguyên tố
 */
public class PrimeServer {

    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "PrimeService";

    private static Registry registry;

    public static void main(String[] args) {
        try {
            PrimeCheckerImpl checker = new PrimeCheckerImpl();

            registry = LocateRegistry.createRegistry(PORT);
            registry.rebind(SERVICE_NAME, checker);

            System.out.println("===== PRIME SERVER =====");
            System.out.println("Server dang chay tai cong " + PORT + ", ten dich vu: " + SERVICE_NAME);
            System.out.println("Dang cho client... (Ctrl + C de tat)");
        } catch (Exception e) {
            System.out.println("Loi server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
