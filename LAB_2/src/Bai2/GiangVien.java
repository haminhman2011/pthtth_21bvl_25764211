public class GiangVien extends Nguoi {

    // Thuộc tính riêng của giảng viên
    private String maGiangVien;
    private String chuyenMon;
    private double luongCoBan;
    private double heSoLuong;

    // Constructor
    public GiangVien(String hoTen, int namSinh, String diaChi,
                     String maGiangVien, String chuyenMon,
                     double luongCoBan, double heSoLuong) {

        // Gọi constructor lớp cha
        super(hoTen, namSinh, diaChi);

        this.maGiangVien = maGiangVien;
        this.chuyenMon = chuyenMon;
        this.luongCoBan = luongCoBan;
        this.heSoLuong = heSoLuong;
    }

    // Getter
    public String getMaGiangVien() {
        return maGiangVien;
    }

    public String getChuyenMon() {
        return chuyenMon;
    }

    public double getLuongCoBan() {
        return luongCoBan;
    }

    public double getHeSoLuong() {
        return heSoLuong;
    }

    // Setter
    public void setMaGiangVien(String maGiangVien) {
        this.maGiangVien = maGiangVien;
    }

    public void setChuyenMon(String chuyenMon) {
        this.chuyenMon = chuyenMon;
    }

    public void setLuongCoBan(double luongCoBan) {
        this.luongCoBan = luongCoBan;
    }

    public void setHeSoLuong(double heSoLuong) {
        this.heSoLuong = heSoLuong;
    }

    // Tính lương
    public double tinhLuong() {
        return luongCoBan * heSoLuong;
    }

    // Ghi đè phương thức hiển thị
    @Override
    public void hienThiThongTin() {

        // Hiển thị thông tin chung
        super.hienThiThongTin();

        // Hiển thị thông tin riêng
        System.out.println("Ma giang vien: " + maGiangVien);
        System.out.println("Chuyen mon: " + chuyenMon);
        System.out.println("Luong co ban: " + luongCoBan);
        System.out.println("He so luong: " + heSoLuong);
        System.out.println("Luong: " + tinhLuong());

        System.out.println("----------------------------");
    }
}
