// Delete thư mục

import java.io.File;

public class Bai8 {

  // TH1: Delete thư mục rỗng
  public boolean deleteEmptyFolder(String source) {
    File folder = new File(source);
    //kiem tra neu folder ton tai thi xoa
    if (folder.exists()) {
      folder.delete();
      System.out.println("folder ton tai\n xoa folder thanh cong");
      return true;
    } else {
      System.out.println("folder khong ton tai");
    }
    return false;
  }

  // TH2: Delete thư mục chứa Files
  public boolean deleteListFileInfolder(String source) {
    File folder = new File(source);
    // folder ton tai
    if (folder.exists()) {
      // danh sach file
      File[] listFile = folder.listFiles();
      if (listFile.length != 0) {
        for (File f : listFile) {
          // file thi xoa
          if (f.isFile()) {
            f.delete();
          }
        }
      }
      folder.delete();
      System.out.println("Delete folder thành công!");
      return true;
    } else {
      System.out.println("folder không tồn tại");
      return false;
    }
  }

  // TH3: Delete thư mục chứa danh sách thư mục con và trong thư mục con chứa danh sách file
  public boolean deleteFolder(String source) {
    File folder = new File(source);
    if (folder.exists()) {
      File[] listFile = folder.listFiles();
      if (listFile != null && listFile.length != 0) {
        for (File f : listFile) {
          if (f.isFile()) {
            f.delete();
          } else {
            // thu muc con thi de quy xoa tiep
            deleteFolder(f.getAbsolutePath());
          }
        }
      }
      folder.delete();
      System.out.println("Delete folder thành công!");
      return true;
    } else {
      System.out.println("folder không tồn tại");
      return false;
    }
  }

  public static void main(String[] args) {
    Bai8 bai8 = new Bai8();
   bai8.deleteEmptyFolder("F:/DHCN4/HK1-2026/Phát triển hệ thống tích hợp/test");
    bai8.deleteListFileInfolder("F:/DHCN4/HK1-2026/Phát triển hệ thống tích hợp/test");
    bai8.deleteFolder("F:/DHCN4/HK1-2026/Phát triển hệ thống tích hợp/test");
  }
}
