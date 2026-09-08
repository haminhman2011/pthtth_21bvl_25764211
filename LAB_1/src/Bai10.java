// Copy File

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Bai10 {

  public boolean copyFile(String source, String dest) throws FileNotFoundException, IOException {
    // file nguon
    File sourceFile = new File(source);
    // file dich
    File destFile = new File(dest);
    // kiem tra file nguon co ton tai khong
    if (sourceFile.exists()) {
      // luong doc file
      FileInputStream fis = new FileInputStream(sourceFile);
      // luong ghi file
      FileOutputStream fos = new FileOutputStream(destFile);
      byte[] arr = new byte[1024];
      while ((fis.read(arr)) != -1) {
        fos.write(arr);
        fos.flush();
      }
      fis.close();
      fos.close();
      System.out.println("copy thành công");
      return true;
    } else {
      System.out.println("file nguồn không tồn tại");
      return false;
    }
  }

  public static void main(String[] args) throws FileNotFoundException, IOException {
    Bai10 bai10 = new Bai10();
    bai10.copyFile("F:/DHCN4/HK1-2026/Phát triển hệ thống tích hợp/test.txt", "F:/DHCN4/HK1-2026/Phát triển hệ thống tích hợp/b.txt");
  }
}
