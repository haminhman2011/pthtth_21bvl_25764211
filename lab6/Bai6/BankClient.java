package Bai6;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * BÀI 6 - Chương trình CLIENT ngân hàng (có menu)
 */
public class BankClient {

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        Scanner sc = new Scanner(System.in);

        try {
            Registry registry = LocateRegistry.getRegistry(host, BankServer.PORT);
            BankAccount stub = (BankAccount) registry.lookup(BankServer.SERVICE_NAME);

            System.out.println("===== BANK CLIENT =====");

            while (true) {
                System.out.println();
                System.out.println("------ MENU ------");
                System.out.println("1. Xem so du");
                System.out.println("2. Gui tien");
                System.out.println("3. Rut tien");
                System.out.println("0. Thoat");
                System.out.print("Chon: ");

                if (!sc.hasNextLine()) break;
                String chon = sc.nextLine().trim();

                if (chon.equals("0")) {
                    System.out.println("Tam biet!");
                    break;
                }

                switch (chon) {
                    case "1":
                        System.out.println("=> So du hien tai: " + BankAccountImpl.dinhDang(stub.getBalance()));
                        break;
                    case "2": {
                        double tien = nhapSoTien(sc, "Nhap so tien muon gui: ");
                        stub.deposit(tien);
                        System.out.println("=> Gui tien thanh cong! So du moi: "
                                + BankAccountImpl.dinhDang(stub.getBalance()));
                        break;
                    }
                    case "3": {
                        double tien = nhapSoTien(sc, "Nhap so tien muon rut: ");
                        if (stub.withdraw(tien)) {
                            System.out.println("=> Rut tien thanh cong! So du moi: "
                                    + BankAccountImpl.dinhDang(stub.getBalance()));
                        } else {
                            System.out.println("=> Rut tien THAT BAI: so du khong du! So du hien tai: "
                                    + BankAccountImpl.dinhDang(stub.getBalance()));
                        }
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

    // Nhập số tiền > 0, sai thì nhập lại
    private static double nhapSoTien(Scanner sc, String thongBao) {
        while (true) {
            System.out.print(thongBao);
            String s = sc.nextLine().trim();
            try {
                double tien = Double.parseDouble(s);
                if (tien > 0) return tien;
                System.out.println("  So tien phai lon hon 0!");
            } catch (NumberFormatException e) {
                System.out.println("  Ban phai nhap so!");
            }
        }
    }
}
