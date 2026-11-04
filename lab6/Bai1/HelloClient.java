package Bai1;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * BÀI 1 - Chương trình CLIENT
 *
 * Các bước:
 *  B1. Kết nối tới Registry của server (theo địa chỉ IP + cổng)
 *  B2. Tìm (lookup) dịch vụ theo tên "HelloService" -> nhận được 1 "stub"
 *  B3. Gọi phương thức trên stub y như gọi hàm bình thường,
 *      thực chất lời gọi được gửi qua mạng tới server.
 */
public class HelloClient {

    public static void main(String[] args) {
        // Mặc định kết nối tới máy hiện tại, có thể truyền IP server qua tham số
        String host = (args.length > 0) ? args[0] : "localhost";

        try {
            // B1: lấy registry của server
            Registry registry = LocateRegistry.getRegistry(host, HelloServer.PORT);

            // B2: tìm đối tượng từ xa, ép kiểu về interface Hello
            Hello stub = (Hello) registry.lookup(HelloServer.SERVICE_NAME);

            // B3: gọi phương thức từ xa
            String ketQua = stub.sayHello();

            System.out.println("===== HELLO CLIENT =====");
            System.out.println("Da ket noi toi server: " + host + ":" + HelloServer.PORT);
            System.out.println("Ket qua nhan tu server: " + ketQua);
        } catch (Exception e) {
            System.out.println("Loi client: " + e.getMessage());
            System.out.println("=> Kiem tra lai xem server da chay chua?");
        }
    }
}
