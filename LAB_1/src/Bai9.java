/**
 *Tìm kiếm file
 *  */

import java.io.File;

public class Bai9 {

  public void finFile(String source, String key) {
    File file = new File(source);
    if (file.exists()) {
      if (file.isFile()) {
        if (file.getName().endsWith(key)) {
          System.out.println(file.getAbsolutePath());
        }
      }
      File[] listFile = file.listFiles();
      if (listFile != null) {
        for (File f : listFile) {
          finFile(f.getAbsolutePath(), key);
        }
      }
    } else {
      System.out.println("source không tồn tại");
    }
  }

  public static void main(String[] args) {
    Bai9 bai9 = new Bai9();
    bai9.finFile("F:/DHCN4/HK1-2026/Phát triển hệ thống tích hợp", ".pdf");
  }
}
