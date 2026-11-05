# LAB 08 – JDBC (Java Database Connectivity) với SQLite

| Thư mục | Nội dung | Lớp chạy (main) | Kiểu chạy |
|---------|----------|-----------------|-----------|
| [HuongDan](HuongDan/README.md) | Hướng dẫn thực hành B1 → B5: bảng `NhanVien` | `HuongDan.NhanVienDemo` | Tự chạy |
| [Bai1](Bai1/README.md) | BT1 – Quản lý sản phẩm CRUD (dùng `Statement`) | `Bai1.SanPhamApp` | Menu |
| [Bai2](Bai2/README.md) | BT2 – Giao dịch (Transaction): commit / rollback | `Bai2.GiaoDichDemo` | Tự chạy |
| [Bai3](Bai3/README.md) | BT3 – Làm lại BT1 với try-catch-finally nghiêm ngặt | `Bai3.SanPhamApp` | Menu |
| [Bai4](Bai4/README.md) | BT4 – `PreparedStatement`: chống SQL Injection + hiệu năng | `Bai4.SanPhamApp` | Menu |
| [Bai5](Bai5/README.md) | BT5 – Hàm tổng hợp SUM / AVG / MIN / MAX / COUNT + GROUP BY | `Bai5.ThongKeLuong` | Tự chạy |

## Thư viện (driver) JDBC
Driver SQLite đã được nhúng sẵn ở **`lib/sqlite-jdbc-3.53.4.0.jar`** (copy từ `167 ShareSV`).
Tất cả các bài dùng chung file `.jar` này. Driver được nạp bằng:
```java
Class.forName("org.sqlite.JDBC");
Connection conn = DriverManager.getConnection("jdbc:sqlite:BaiX/ten.db");
```
SQLite không cần cài server: file `.db` tự được tạo ra trong thư mục của từng bài ở lần chạy đầu tiên.

## Cách chạy chung (bài nào cũng giống nhau)
Luôn mở terminal ở **thư mục `lab8`** (vì mỗi file có `package BaiX;` và đường dẫn CSDL là `BaiX/...db`).

**Windows (PowerShell / CMD)** – dấu phân cách classpath là `;`
```bash
javac -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" BaiX/*.java
java --enable-native-access=ALL-UNNAMED -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" BaiX.<TenLop>
```

**Linux / macOS / Git Bash** – dấu phân cách là `:` (Git Bash trên Windows vẫn dùng `;`)
```bash
javac -cp ".:lib/sqlite-jdbc-3.53.4.0.jar" BaiX/*.java
java --enable-native-access=ALL-UNNAMED -cp ".:lib/sqlite-jdbc-3.53.4.0.jar" BaiX.<TenLop>
```

Ví dụ chạy phần hướng dẫn:
```bash
javac -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" HuongDan/*.java
java --enable-native-access=ALL-UNNAMED -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" HuongDan.NhanVienDemo
```

> - `-cp` (classpath) gồm 2 phần: `.` (thư mục chứa các package `BaiX`) và file `.jar` của driver. **Thiếu file jar** → lỗi `ClassNotFoundException: org.sqlite.JDBC` hoặc `No suitable driver found`.
> - `--enable-native-access=ALL-UNNAMED` chỉ để tắt dòng `WARNING: A restricted method...` trên Java 24+ (driver SQLite nạp thư viện native). Bỏ cờ này chương trình vẫn chạy bình thường. Java 8–21 thì **không** dùng cờ này.

### Chạy bằng nút Run trong VS Code
Mở **thư mục `lab8`** làm workspace. File [.vscode/settings.json](.vscode/settings.json) đã cấu hình sẵn:
- `java.project.referencedLibraries` → tự thêm `lib/*.jar` vào classpath,
- `java.debug.settings.vmArgs` → tự thêm cờ `--enable-native-access`.

Mở file có hàm `main` → bấm **Run**.

## Các bước làm 1 chương trình JDBC
1. **Nạp driver + kết nối**: `Class.forName(...)` → `DriverManager.getConnection(url)`.
2. **Tạo Statement / PreparedStatement** từ `Connection`.
3. **Thực thi**: `executeUpdate()` cho CREATE / INSERT / UPDATE / DELETE (trả về số dòng), `executeQuery()` cho SELECT (trả về `ResultSet`).
4. **Duyệt kết quả**: `while (rs.next()) { rs.getInt("id"); rs.getString("ten"); ... }`.
5. **Đóng tài nguyên** theo thứ tự ngược lại `rs → stmt → conn`, trong `finally` hoặc dùng try-with-resources.
6. **Giao dịch**: `conn.setAutoCommit(false)` → các lệnh → `conn.commit()`; có lỗi thì `conn.rollback()` trong `catch`.

## Lỗi thường gặp
| Lỗi | Nguyên nhân / cách sửa |
|-----|------------------------|
| `ClassNotFoundException: org.sqlite.JDBC` | Thiếu `lib/sqlite-jdbc-3.53.4.0.jar` trong `-cp` |
| `Could not find or load main class` | Đứng sai thư mục (phải ở `lab8`) hoặc quên tiền tố `BaiX.` |
| `no such table` / file `.db` bị tạo ở chỗ lạ | Chạy từ thư mục khác `lab8` nên đường dẫn `BaiX/*.db` bị lệch |
| `[SQLITE_BUSY] database is locked` | Đang mở file `.db` bằng chương trình khác (DB Browser…) hoặc 2 chương trình cùng ghi |
| `PRIMARY KEY constraint failed` | Thêm sản phẩm trùng `maSP` |
| Muốn làm lại dữ liệu từ đầu | Xoá file `BaiX/*.db` rồi chạy lại |
