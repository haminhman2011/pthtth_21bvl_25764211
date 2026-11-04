package Bai8;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * BÀI 8 - Chương trình CLIENT đặt vé máy bay (có menu)
 */
public class FlightClient {

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        Scanner sc = new Scanner(System.in);

        try {
            Registry registry = LocateRegistry.getRegistry(host, FlightServer.PORT);
            FlightBooking stub = (FlightBooking) registry.lookup(FlightServer.SERVICE_NAME);

            System.out.println("===== FLIGHT CLIENT =====");

            while (true) {
                System.out.println();
                System.out.println("------ MENU ------");
                System.out.println("1. Xem danh sach chuyen bay");
                System.out.println("2. Kiem tra so ghe trong");
                System.out.println("3. Dat ve");
                System.out.println("4. Huy ve");
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
                        System.out.println("=== DANH SACH CHUYEN BAY ===");
                        System.out.print(stub.listFlights());
                        break;
                    case "2": {
                        String ma = nhapMaChuyenBay(sc);
                        int conTrong = stub.availableSeats(ma);
                        if (conTrong < 0) {
                            System.out.println("=> Khong co chuyen bay " + ma);
                        } else {
                            System.out.println("=> Chuyen " + ma + " con " + conTrong + " ghe trong");
                        }
                        break;
                    }
                    case "3": {
                        String ma = nhapMaChuyenBay(sc);
                        int soGhe = nhapSoGhe(sc);
                        if (stub.bookTicket(ma, soGhe)) {
                            System.out.println("=> Dat " + soGhe + " ghe thanh cong! Con lai: " + stub.availableSeats(ma) + " ghe");
                        } else {
                            System.out.println("=> Dat ve THAT BAI (sai ma chuyen bay hoac khong du ghe trong)");
                        }
                        break;
                    }
                    case "4": {
                        String ma = nhapMaChuyenBay(sc);
                        int soGhe = nhapSoGhe(sc);
                        if (stub.cancelBooking(ma, soGhe)) {
                            System.out.println("=> Huy " + soGhe + " ghe thanh cong! Con lai: " + stub.availableSeats(ma) + " ghe");
                        } else {
                            System.out.println("=> Huy ve THAT BAI (sai ma chuyen bay hoac huy nhieu hon so ghe da dat)");
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

    private static String nhapMaChuyenBay(Scanner sc) {
        while (true) {
            System.out.print("Nhap ma chuyen bay (vd: VN123): ");
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("  Khong duoc de trong!");
        }
    }

    private static int nhapSoGhe(Scanner sc) {
        while (true) {
            System.out.print("Nhap so ghe: ");
            String s = sc.nextLine().trim();
            try {
                int n = Integer.parseInt(s);
                if (n > 0) return n;
                System.out.println("  So ghe phai lon hon 0!");
            } catch (NumberFormatException e) {
                System.out.println("  Ban phai nhap so nguyen!");
            }
        }
    }
}
