// Đọc và ghi File ảnh - xem Class_Images.java

import java.io.File;

public class Bai13 {

  public static void main(String[] args) {
    // doc file anh nguon thanh mang byte
    File source = new File("F:/DHCN4/HK1-2026/Phát triển hệ thống tích hợp/anh.jpg");
    byte[] bfile = Class_Images.readFile(source);

    // ghi mang byte do ra file anh dich
    File dest = new File("F:/DHCN4/HK1-2026/Phát triển hệ thống tích hợp/anh_copy.jpg");
    Class_Images.saveFile(dest, "jpg", bfile);
  }
}
