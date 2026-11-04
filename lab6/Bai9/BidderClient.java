package Bai9;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * BÀI 9 - CLIENT cho Người đấu giá (Bidder)
 * Chức năng:
 *  - Xem danh sách sản phẩm đang đấu giá
 *  - Đặt giá thầu cao hơn giá hiện tại
 *  - Nhận thông báo thời gian thực khi có người trả giá cao hơn (nhờ callback)
 */
public class BidderClient {

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        Scanner sc = new Scanner(System.in);

        try {
            Registry registry = LocateRegistry.getRegistry(host, AuctionServer.PORT);
            AuctionService service = (AuctionService) registry.lookup(AuctionServer.SERVICE_NAME);

            System.out.println("===== BIDDER CLIENT =====");
            String bidder = SellerClient.nhapChuoi(sc, "Nhap ten nguoi dau gia: ");

            // Đăng ký nhận thông báo từ server
            AuctionListenerImpl listener = new AuctionListenerImpl();
            service.registerListener(listener);
            System.out.println("Da dang ky nhan thong bao tu server.");

            while (true) {
                System.out.println();
                System.out.println("------ MENU BIDDER (" + bidder + ") ------");
                System.out.println("1. Xem danh sach san pham");
                System.out.println("2. Dat gia thau");
                System.out.println("0. Thoat");
                System.out.print("Chon: ");

                if (!sc.hasNextLine()) break;
                String chon = sc.nextLine().trim();

                if (chon.equals("0")) {
                    break;
                }

                switch (chon) {
                    case "1":
                        SellerClient.inDanhSach(service.getItems());
                        break;
                    case "2": {
                        int id = nhapMaSanPham(sc);
                        double gia = SellerClient.nhapSoTien(sc, "Nhap gia thau: ");
                        if (service.placeBid(id, bidder, gia)) {
                            System.out.println("=> Dat gia thanh cong! Ban dang la nguoi tra gia cao nhat.");
                        } else {
                            System.out.println("=> Dat gia THAT BAI (sai ma san pham hoac gia khong cao hon gia hien tai)");
                        }
                        break;
                    }
                    default:
                        System.out.println("Lua chon khong hop le!");
                }
            }

            service.unregisterListener(listener);
            System.out.println("Tam biet!");
            // Thoát hẳn vì listener đang được export nên JVM chưa tự tắt
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Loi client: " + e.getMessage());
            System.out.println("=> Kiem tra lai xem server da chay chua?");
            System.exit(1);
        }
    }

    private static int nhapMaSanPham(Scanner sc) {
        while (true) {
            System.out.print("Nhap ma san pham (so sau dau #): ");
            String s = sc.nextLine().trim().replace("#", "");
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("  Ban phai nhap so!");
            }
        }
    }
}
