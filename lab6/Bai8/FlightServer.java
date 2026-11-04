package Bai8;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * BÀI 8 - Chương trình SERVER đặt vé máy bay
 */
public class FlightServer {

    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "FlightService";

    private static Registry registry;

    public static void main(String[] args) {
        try {
            FlightBookingImpl booking = new FlightBookingImpl();

            registry = LocateRegistry.createRegistry(PORT);
            registry.rebind(SERVICE_NAME, booking);

            System.out.println("===== FLIGHT SERVER =====");
            System.out.println("Server dang chay tai cong " + PORT + ", ten dich vu: " + SERVICE_NAME);
            System.out.println("Danh sach chuyen bay:");
            System.out.print(booking.listFlights());
            System.out.println("Dang cho client... (Ctrl + C de tat)");
        } catch (Exception e) {
            System.out.println("Loi server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
