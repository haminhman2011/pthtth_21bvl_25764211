public class MonHoc {
  private String tenMonHoc;
  private int tinChi;
  private double diem;

  public MonHoc(String tenMonHoc, int tinChi, double diem) {
    this.tenMonHoc = tenMonHoc;
    this.tinChi = tinChi;
    this.diem = diem;
  }

  public String getTenMonHoc() {
    return tenMonHoc;
  }

  public void setTenMonHoc(String tenMonHoc) {
    this.tenMonHoc = tenMonHoc;
  }

  public int getTinChi() {
    return tinChi;
  }

  public void setTinChi(int tinChi) {
    this.tinChi = tinChi;
  }

  public double getDiem() {
    return diem;
  }

  public void setDiem(double diem) {
    this.diem = diem;
  }

  @Override
  public String toString() {
    return "MonHoc [tenMonHoc=" + tenMonHoc + ", tinChi=" + tinChi + ", diem=" + diem + "]";
  }
}
