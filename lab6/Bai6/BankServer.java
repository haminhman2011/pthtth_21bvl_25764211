package Bai6;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * BÀI 6 - Chương trình SERVER ngân hàng
 * Tài khoản có số dư ban đầu là 1,000,000 VND.
 */
public class BankServer {

    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "BankService";
    public static final double SO_DU_BAN_DAU = 1_000_000;

    private static Registry registry;

    public static void main(String[] args) {
        try {
            BankAccountImpl account = new BankAccountImpl(SO_DU_BAN_DAU);

            registry = LocateRegistry.createRegistry(PORT);
            registry.rebind(SERVICE_NAME, account);

            System.out.println("===== BANK SERVER =====");
            System.out.println("Server dang chay tai cong " + PORT + ", ten dich vu: " + SERVICE_NAME);
            System.out.println("So du ban dau: " + BankAccountImpl.dinhDang(SO_DU_BAN_DAU));
            System.out.println("Dang cho client... (Ctrl + C de tat)");
        } catch (Exception e) {
            System.out.println("Loi server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
