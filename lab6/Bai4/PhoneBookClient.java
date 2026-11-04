package Bai4;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * BÀI 4 - Chương trình CLIENT quản lý danh bạ (có menu)
 */
public class PhoneBookClient {

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        Scanner sc = new Scanner(System.in);

        try {
            Registry registry = LocateRegistry.getRegistry(host, PhoneBookServer.PORT);
            PhoneBook stub = (PhoneBook) registry.lookup(PhoneBookServer.SERVICE_NAME);

            System.out.println("===== PHONE BOOK CLIENT =====");

            while (true) {
                System.out.println();
                System.out.println("------ MENU ------");
                System.out.println("1. Them lien he");
                System.out.println("2. Tim lien he");
                System.out.println("3. Sua so dien thoai");
                System.out.println("4. Xoa lien he");
                System.out.println("5. Xem toan bo danh ba");
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
                        String name = nhapChuoi(sc, "Nhap ten: ");
                        String phone = nhapChuoi(sc, "Nhap so dien thoai: ");
                        stub.addContact(name, phone);
                        System.out.println("=> Da them lien he " + name);
                        break;
                    }
                    case "2": {
                        String name = nhapChuoi(sc, "Nhap ten can tim: ");
                        String phone = stub.findContact(name);
                        if (phone == null) {
                            System.out.println("=> Khong tim thay " + name);
                        } else {
                            System.out.println("=> " + name + ": " + phone);
                        }
                        break;
                    }
                    case "3": {
                        String name = nhapChuoi(sc, "Nhap ten can sua: ");
                        String phone = nhapChuoi(sc, "Nhap so dien thoai moi: ");
                        if (stub.updateContact(name, phone)) {
                            System.out.println("=> Da sua so cua " + name);
                        } else {
                            System.out.println("=> Khong co ten " + name + " trong danh ba");
                        }
                        break;
                    }
                    case "4": {
                        String name = nhapChuoi(sc, "Nhap ten can xoa: ");
                        if (stub.deleteContact(name)) {
                            System.out.println("=> Da xoa " + name);
                        } else {
                            System.out.println("=> Khong co ten " + name + " trong danh ba");
                        }
                        break;
                    }
                    case "5":
                        System.out.println("=== DANH BA ===");
                        System.out.print(stub.getAllContacts());
                        System.out.println();
                        break;
                    default:
                        System.out.println("Lua chon khong hop le!");
                }
            }
        } catch (Exception e) {
            System.out.println("Loi client: " + e.getMessage());
            System.out.println("=> Kiem tra lai xem server da chay chua?");
        }
    }

    // Nhập chuỗi không được để trống
    private static String nhapChuoi(Scanner sc, String thongBao) {
        while (true) {
            System.out.print(thongBao);
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("  Khong duoc de trong!");
        }
    }
}
