package Bai2;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * BÀI 2 - Chương trình CLIENT
 * Nhập 2 số nguyên từ bàn phím, gửi lên server và in ra tổng.
 */
public class CalculatorClient {

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        Scanner sc = new Scanner(System.in);

        try {
            // Kết nối registry và tìm dịch vụ
            Registry registry = LocateRegistry.getRegistry(host, CalculatorServer.PORT);
            Calculator stub = (Calculator) registry.lookup(CalculatorServer.SERVICE_NAME);

            System.out.println("===== CALCULATOR CLIENT =====");
            System.out.println("Da ket noi toi server " + host);

            String tiepTuc;
            do {
                int a = nhapSoNguyen(sc, "Nhap so a: ");
                int b = nhapSoNguyen(sc, "Nhap so b: ");

                // Gọi phương thức từ xa
                int tong = stub.add(a, b);
                System.out.println("=> Ket qua tu server: " + a + " + " + b + " = " + tong);

                System.out.print("Ban co muon tinh tiep khong? (c/k): ");
                tiepTuc = sc.hasNextLine() ? sc.nextLine().trim() : "k";
            } while (tiepTuc.equalsIgnoreCase("c"));

            System.out.println("Tam biet!");
        } catch (Exception e) {
            System.out.println("Loi client: " + e.getMessage());
            System.out.println("=> Kiem tra lai xem server da chay chua?");
        }
    }

    // Hàm nhập số nguyên, nhập sai thì bắt nhập lại
    private static int nhapSoNguyen(Scanner sc, String thongBao) {
        while (true) {
            System.out.print(thongBao);
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("  Ban phai nhap so nguyen! Nhap lai.");
            }
        }
    }
}
