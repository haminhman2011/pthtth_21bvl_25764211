package Bai9;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * BÀI 9 - Chương trình SERVER đấu giá
 */
public class AuctionServer {

    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "AuctionService";

    private static Registry registry;

    public static void main(String[] args) {
        try {
            AuctionServiceImpl service = new AuctionServiceImpl();

            registry = LocateRegistry.createRegistry(PORT);
            registry.rebind(SERVICE_NAME, service);

            System.out.println("===== AUCTION SERVER =====");
            System.out.println("Server dang chay tai cong " + PORT + ", ten dich vu: " + SERVICE_NAME);
            System.out.println("Dang cho Seller va Bidder ket noi... (Ctrl + C de tat)");
        } catch (Exception e) {
            System.out.println("Loi server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
