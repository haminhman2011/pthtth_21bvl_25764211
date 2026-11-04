package Bai3;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * BÀI 3 - Chương trình CLIENT
 * Nhập 1 số, gửi lên server kiểm tra có phải số nguyên tố không.
 * Gõ "q" để thoát (không dùng số 0 để thoát vì 0 cũng là số hợp lệ cần kiểm tra).
 */
public class PrimeClient {

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        Scanner sc = new Scanner(System.in);

        try {
            Registry registry = LocateRegistry.getRegistry(host, PrimeServer.PORT);
            PrimeChecker stub = (PrimeChecker) registry.lookup(PrimeServer.SERVICE_NAME);

            System.out.println("===== PRIME CLIENT =====");
            System.out.println("Nhap so can kiem tra (go 'q' de thoat)");

            while (true) {
                System.out.print("Nhap so: ");
                if (!sc.hasNextLine()) break;
                String s = sc.nextLine().trim();

                if (s.equalsIgnoreCase("q")) break;

                int n;
                try {
                    n = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    System.out.println("  Ban phai nhap so nguyen!");
                    continue;
                }

                // Gọi phương thức từ xa
                boolean laNguyenTo = stub.isPrime(n);
                if (laNguyenTo) {
                    System.out.println("=> " + n + " LA so nguyen to");
                } else {
                    System.out.println("=> " + n + " KHONG PHAI so nguyen to");
                }
            }
            System.out.println("Tam biet!");
        } catch (Exception e) {
            System.out.println("Loi client: " + e.getMessage());
            System.out.println("=> Kiem tra lai xem server da chay chua?");
        }
    }
}
