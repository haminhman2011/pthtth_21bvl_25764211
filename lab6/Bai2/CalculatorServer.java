package Bai2;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * BÀI 2 - Chương trình SERVER tính tổng
 */
public class CalculatorServer {

    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "CalculatorService";

    private static Registry registry; // giữ static để không bị dọn rác

    public static void main(String[] args) {
        try {
            // Tạo đối tượng từ xa
            CalculatorImpl calculator = new CalculatorImpl();

            // Tạo registry và đăng ký dịch vụ
            registry = LocateRegistry.createRegistry(PORT);
            registry.rebind(SERVICE_NAME, calculator);

            System.out.println("===== CALCULATOR SERVER =====");
            System.out.println("Server dang chay tai cong " + PORT + ", ten dich vu: " + SERVICE_NAME);
            System.out.println("Dang cho client... (Ctrl + C de tat)");
        } catch (Exception e) {
            System.out.println("Loi server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
