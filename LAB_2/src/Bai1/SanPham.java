public class SanPham {
    // 1. Các thuộc tính - sử dụng private để thể hiện tính đóng gói
    private String maSanPham;
    private String tenSanPham;
    private double donGia;
    private int soLuong;

    // 2. Constructor đầy đủ tham số
    public SanPham(String maSanPham, String tenSanPham, double donGia, int soLuong) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.donGia = donGia;
        this.soLuong = soLuong;
    }

    // 3. Getter
    public String getMaSanPham() {
        return maSanPham;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public double getDonGia() {
        return donGia;
    }

    public int getSoLuong() {
        return soLuong;
    }

    // 4. Tính số lượng
    public double tinhThanhTien() {
        return soLuong * donGia;
    }

    // 5. Nhập hàng
    public void nhapHang(int soLuongNhap) {
        if(soLuongNhap > 0) {
            soLuong += soLuongNhap;
            System.out.println("Nhap thanh cong");
        } else {
            System.out.println("So luong nhap phai > hon 0");
        }
    }

    // 6. Bán hàng
    public boolean banHang(int soLuongBan) {
        if(soLuongBan <= 0) {
            System.out.println("so luong ban khong nho hon 0");
            return false;
        }
        // Kiếm tra số lượng bán lớn hơn tồn to
        if(soLuongBan > soLuong) {
            System.out.println("Số lượng bán không được vượt quá số lượng trong kho hiện tại");
            return false;
        }

        // Bán thành công và cập nhật lại trong kho
        soLuong -= soLuongBan;
        System.out.println("Bán thành công");
        return true;
    }

    // 7. Hiển thị thông tin sản phẩm
    public void hienThiThongTinSanPham() {
        System.out.println("----- THONG TIN SAN PHAM -----");
        System.out.println("Ma san pham: " + maSanPham);
        System.out.println("Ten san pham: " + tenSanPham);
        System.out.println("Don gia: " + donGia);
        System.out.println("So luong ton kho: " + soLuong);
        System.out.println("Thanh tien: " + tinhThanhTien());
        System.out.println("------------------------------");
    }





















}
