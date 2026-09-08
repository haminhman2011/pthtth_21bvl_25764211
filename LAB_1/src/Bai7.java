//Delete	file	trong	Java

import java.io.File;
public class Bai7 {
  private void deleteFile(String source) {
    File file = new File(source);
    if(file.exists()){
      System.out.println("File ton tai");
      file.delete();
      System.out.println("Xoa file thanh cong");
    } else {
      System.out.println("File khong ton tai");
    }
  }

  public static void main(String[] args) {
    Bai7 bai7 = new Bai7();
    bai7.deleteFile("F:/DHCN4/HK1-2026/Phát triển hệ thống tích hợp/167 ShareSV/test.txt");
  }
}
