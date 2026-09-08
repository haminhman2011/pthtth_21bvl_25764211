import java.util.ArrayList;

public class SinhVien {
  private String mssv;
  private String ten;
  private int tuoi;
  private ArrayList<MonHoc> listMH;

  public SinhVien(String mssv, String ten, int tuoi, ArrayList<MonHoc> listMH) {
    this.mssv = mssv;
    this.ten = ten;
    this.tuoi = tuoi;
    this.listMH = listMH;
  }

  public String getMssv() {
    return mssv;
  }

  public void setMssv(String mssv) {
    this.mssv = mssv;
  }

  public String getTen() {
    return ten;
  }

  public void setTen(String ten) {
    this.ten = ten;
  }

  public int getTuoi() {
    return tuoi;
  }

  public void setTuoi(int tuoi) {
    this.tuoi = tuoi;
  }

  public ArrayList<MonHoc> getListMH() {
    return listMH;
  }

  public void setListMH(ArrayList<MonHoc> listMH) {
    this.listMH = listMH;
  }

  @Override
  public String toString() {
    return "SinhVien [mssv=" + mssv + ", ten=" + ten + ", tuoi=" + tuoi + ", listMH=" + listMH + "]";
  }
}
