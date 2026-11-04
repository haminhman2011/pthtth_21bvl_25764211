package Bai9;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

/**
 * BÀI 9 - CLIENT cho Chủ sở hữu (Seller)
 * Chức năng: thêm sản phẩm đấu giá, xem danh sách sản phẩm.
 */
public class SellerClient {

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        Scanner sc = new Scanner(System.in);

        try {
            Registry registry = LocateRegistry.getRegistry(host, AuctionServer.PORT);
            AuctionService service = (AuctionService) registry.lookup(AuctionServer.SERVICE_NAME);

            System.out.println("===== SELLER CLIENT =====");
            String seller = nhapChuoi(sc, "Nhap ten nguoi ban: ");

            while (true) {
                System.out.println();
                System.out.println("------ MENU SELLER ------");
                System.out.println("1. Them san pham dau gia");
                System.out.println("2. Xem danh sach san pham");
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
                        String ten = nhapChuoi(sc, "Ten san pham: ");
                        double gia = nhapSoTien(sc, "Gia khoi diem: ");
                        int id = service.addItem(seller, ten, gia);
                        System.out.println("=> Da them san pham, ma so: #" + id);
                        break;
                    }
                    case "2":
                        inDanhSach(service.getItems());
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

    static void inDanhSach(List<AuctionItem> ds) {
        System.out.println("=== DANH SACH SAN PHAM DANG DAU GIA ===");
        if (ds.isEmpty()) {
            System.out.println("(Chua co san pham nao)");
            return;
        }
        for (AuctionItem item : ds) {
            System.out.println(item);
        }
    }

    static String nhapChuoi(Scanner sc, String thongBao) {
        while (true) {
            System.out.print(thongBao);
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("  Khong duoc de trong!");
        }
    }

    static double nhapSoTien(Scanner sc, String thongBao) {
        while (true) {
            System.out.print(thongBao);
            String s = sc.nextLine().trim();
            try {
                double x = Double.parseDouble(s);
                if (x > 0) return x;
                System.out.println("  So tien phai lon hon 0!");
            } catch (NumberFormatException e) {
                System.out.println("  Ban phai nhap so!");
            }
        }
    }
}
