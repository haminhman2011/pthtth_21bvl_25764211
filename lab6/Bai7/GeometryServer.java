package Bai7;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * BÀI 7 - Chương trình SERVER tính diện tích
 */
public class GeometryServer {

    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "GeometryService";

    private static Registry registry;

    public static void main(String[] args) {
        try {
            GeometryImpl geometry = new GeometryImpl();

            registry = LocateRegistry.createRegistry(PORT);
            registry.rebind(SERVICE_NAME, geometry);

            System.out.println("===== GEOMETRY SERVER =====");
            System.out.println("Server dang chay tai cong " + PORT + ", ten dich vu: " + SERVICE_NAME);
            System.out.println("Dang cho client... (Ctrl + C de tat)");
        } catch (Exception e) {
            System.out.println("Loi server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
