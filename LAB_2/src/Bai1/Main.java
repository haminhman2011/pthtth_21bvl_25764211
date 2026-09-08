public class Main {
    public static void main(String[] args) throws Exception {
        // ========================================
        // 1. Tạo hai sản phẩm
        // ========================================

        SanPham sp1 = new SanPham("SP01", "Lap dell", 15000000, 10);
        SanPham sp2 = new SanPham("SP02", "Chuot dell", 500000, 20);

        // ========================================
        // 2. Hiển thị thông tin ban đầu
        // ========================================

        System.out.println("===== THONG TIN BAN DAU =====");
        sp1.hienThiThongTinSanPham();
        sp2.hienThiThongTinSanPham();

        // ========================================
        // 3. Nhập thêm hàng cho sản phẩm 1
        // ========================================

        System.out.println("\n===== NHAP THEM HANG CHO SP01 =====");

        System.out.println("Truoc khi nhap hang:");
        sp1.hienThiThongTinSanPham();

        System.out.println("Nhap them 5 san pham:");
        sp1.nhapHang(5);

        System.out.println("Sau khi nhap hang:");
        sp1.hienThiThongTinSanPham();

        // ========================================
        // 4. Thử bán hàng thành công
        // ========================================

        System.out.println("\n===== BAN HANG THANH CONG =====");

        System.out.println("Truoc khi ban:");
        sp1.hienThiThongTinSanPham();

        System.out.println("Ban 3 san pham:");

        boolean ketQua1 = sp1.banHang(3);

        System.out.println("Ket qua ban hang: " + ketQua1);

        System.out.println("Sau khi ban:");
        sp1.hienThiThongTinSanPham();


        // ========================================
        // 5. Thử bán vượt quá tồn kho
        // ========================================

        System.out.println("\n===== BAN VUOT QUA TON KHO =====");

        System.out.println("Truoc khi ban:");
        sp1.hienThiThongTinSanPham();

        System.out.println("Thu ban 100 san pham:");

        boolean ketQua2 = sp1.banHang(100);

        System.out.println("Ket qua ban hang: " + ketQua2);

        System.out.println("Sau khi ban:");
        sp1.hienThiThongTinSanPham();

    }
}
