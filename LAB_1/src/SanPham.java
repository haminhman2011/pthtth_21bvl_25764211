public class SanPham {

    private String maSP;
    private String tenSP;
    private double donGia;
    private double soLuong;

    // Constructor
    public SanPham(String maSP, String tenSP, double donGia, double soLuong) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.donGia = donGia;
        this.soLuong = soLuong;
    }

    // Tính thành tiền
    double tinhThanhTien() {
        return donGia * soLuong;
    }

    // Hiển thị thông tin sản phẩm
    public void hienThiThongTin() {
        System.out.println("maSP : " + maSP);
        System.out.println("tenSP : " + tenSP);
        System.out.println("soLuong = " + soLuong + ", donGia = " + donGia);
        System.out.println("Tong thanh tien = " + tinhThanhTien());
    }

    // Getter
    public String getTenSP() {
        return tenSP;
    }

    // Setter
    public void setTenSP(String tenSP) {
        this.tenSP = tenSP;
    }

    public static void main(String[] args) {

        SanPham sp1 = new SanPham("SP001", "iPhone 15 promax", 34000, 2);

        sp1.hienThiThongTin();

        sp1.setTenSP("Galaxy");

        System.out.println("doi ten sp : " + sp1.getTenSP());
    }
}