package Bai7;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * BÀI 7 - Chương trình CLIENT tính diện tích (có menu)
 */
public class GeometryClient {

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        Scanner sc = new Scanner(System.in);

        try {
            Registry registry = LocateRegistry.getRegistry(host, GeometryServer.PORT);
            Geometry stub = (Geometry) registry.lookup(GeometryServer.SERVICE_NAME);

            System.out.println("===== GEOMETRY CLIENT =====");

            while (true) {
                System.out.println();
                System.out.println("------ MENU ------");
                System.out.println("1. Dien tich hinh chu nhat");
                System.out.println("2. Dien tich hinh tron");
                System.out.println("3. Dien tich hinh tam giac");
                System.out.println("0. Thoat");
                System.out.print("Chon: ");

                if (!sc.hasNextLine()) break;
                String chon = sc.nextLine().trim();

                if (chon.equals("0")) {
                    System.out.println("Tam biet!");
                    break;
                }

                switch (chon) {
                    case "1": {
                        double w = nhapSoDuong(sc, "Nhap chieu dai (width): ");
                        double h = nhapSoDuong(sc, "Nhap chieu rong (height): ");
                        System.out.printf("=> Dien tich hinh chu nhat = %.2f%n", stub.rectangleArea(w, h));
                        break;
                    }
                    case "2": {
                        double r = nhapSoDuong(sc, "Nhap ban kinh: ");
                        System.out.printf("=> Dien tich hinh tron = %.2f%n", stub.circleArea(r));
                        break;
                    }
                    case "3": {
                        double b = nhapSoDuong(sc, "Nhap canh day: ");
                        double h = nhapSoDuong(sc, "Nhap chieu cao: ");
                        System.out.printf("=> Dien tich tam giac = %.2f%n", stub.triangleArea(b, h));
                        break;
                    }
                    default:
                        System.out.println("Lua chon khong hop le!");
                }
            }
        } catch (Exception e) {
            System.out.println("Loi client: " + e.getMessage());
            System.out.println("=> Kiem tra lai xem server da chay chua?");
        }
    }

    // Nhập số thực > 0 (độ dài không thể âm), sai thì nhập lại
    private static double nhapSoDuong(Scanner sc, String thongBao) {
        while (true) {
            System.out.print(thongBao);
            String s = sc.nextLine().trim();
            try {
                double x = Double.parseDouble(s);
                if (x > 0) return x;
                System.out.println("  Gia tri phai lon hon 0!");
            } catch (NumberFormatException e) {
                System.out.println("  Ban phai nhap so!");
            }
        }
    }
}
