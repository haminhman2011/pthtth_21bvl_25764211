public class Main {

    public static void main(String[] args) {

        // =====================================
        // 1. Tạo 2 sinh viên
        // =====================================

        SinhVien sv1 = new SinhVien(
                "Nguyen Van An",
                2005,
                "TP.HCM",
                "SV01",
                "Cong nghe thong tin",
                8.7
        );

        SinhVien sv2 = new SinhVien(
                "Tran Thi Binh",
                2004,
                "Dong Nai",
                "SV02",
                "Ke toan",
                6.5
        );


        // =====================================
        // 2. Tạo 2 giảng viên
        // =====================================

        GiangVien gv1 = new GiangVien(
                "Nguyen Van Minh",
                1980,
                "TP.HCM",
                "GV01",
                "Lap trinh Java",
                5000000,
                2.5
        );

        GiangVien gv2 = new GiangVien(
                "Le Thi Hoa",
                1985,
                "Ha Noi",
                "GV02",
                "Co so du lieu",
                6000000,
                2.2
        );


        // =====================================
        // 3. Hiển thị sinh viên 1
        // =====================================

        System.out.println("========== SINH VIEN 1 ==========");

        sv1.hienThiThongTin();

        System.out.println("Xep loai: " + sv1.xepLoai());


        // =====================================
        // 4. Hiển thị sinh viên 2
        // =====================================

        System.out.println("\n========== SINH VIEN 2 ==========");

        sv2.hienThiThongTin();

        System.out.println("Xep loai: " + sv2.xepLoai());


        // =====================================
        // 5. Hiển thị giảng viên 1
        // =====================================

        System.out.println("\n========== GIANG VIEN 1 ==========");

        gv1.hienThiThongTin();

        System.out.println("Luong: " + gv1.tinhLuong());


        // =====================================
        // 6. Hiển thị giảng viên 2
        // =====================================

        System.out.println("\n========== GIANG VIEN 2 ==========");

        gv2.hienThiThongTin();

        System.out.println("Luong: " + gv2.tinhLuong());
    }
}
