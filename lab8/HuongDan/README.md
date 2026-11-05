# Hướng dẫn thực hành – Bảng NhanVien (B1 → B5)

## Đề bài
Làm theo đúng 5 bước trong tài liệu với CSDL SQLite `nhanvien.db`:
- **B1.** Thêm driver `sqlite-jdbc`, nạp driver và kết nối.
- **B2.** Tạo `Statement` từ `Connection`.
- **B3.** Tạo bảng `NhanVien(id INTEGER PRIMARY KEY, ten TEXT NOT NULL, chuc_vu TEXT)` và thêm ít nhất 3 nhân viên bằng `INSERT INTO`.
- **B4.** `SELECT` tất cả nhân viên, duyệt `ResultSet` bằng `while (rs.next())` và in ra console.
- **B5.** Đóng `rs → stmt → conn` theo thứ tự ngược lại.
- **Xử lý ngoại lệ:** bọc trong `try-catch(SQLException)`, đóng tài nguyên trong `finally`, các lệnh `INSERT` chạy trong **1 giao dịch** (`setAutoCommit(false)` → `commit()`, lỗi thì `rollback()`).

## File trong bài
| File | Vai trò |
|------|---------|
| `NhanVienDemo.java` | Toàn bộ B1 → B5, có đánh dấu `[B1]`, `[B2]`… khi in ra |
| `nhanvien.db` | File CSDL, **tự sinh** khi chạy (mỗi lần chạy sẽ xoá bảng cũ và tạo lại) |

## Kiến thức chính
```java
Class.forName("org.sqlite.JDBC");                                  // B1
Connection conn = DriverManager.getConnection(url, username, passwd);
Statement stmt = conn.createStatement();                           // B2
stmt.executeUpdate(sql_create_table);                              // B3
ResultSet rs = stmt.executeQuery(sql_select);                      // B4
while (rs.next()) { rs.getInt("id"); rs.getString("ten"); }
rs.close(); stmt.close(); conn.close();                            // B5 (trong finally)
```
SQLite không có tài khoản nên `username`, `passwd` để chuỗi rỗng.

## Cách chạy
Mở terminal ở **thư mục `lab8`** (KHÔNG `cd` vào `HuongDan`):
```bash
javac -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" HuongDan/*.java
java --enable-native-access=ALL-UNNAMED -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" HuongDan.NhanVienDemo
```
(Linux/macOS thay `;` bằng `:`.)

## Kết quả mẫu
```
[B1] Ket noi thanh cong: jdbc:sqlite:HuongDan/nhanvien.db
[B2] Da tao Statement
[B3.1] Da tao bang NhanVien
[B3.2] Da them 4 nhan vien (commit)
[B4] Danh sach nhan vien:
+----+----------------------+------------------+
| ID | Ten                  | Chuc vu          |
+----+----------------------+------------------+
| 1  | Nguyen Van An        | Giam doc         |
| 2  | Tran Thi Binh        | Ke toan          |
| 3  | Le Van Cuong         | Lap trinh vien   |
| 4  | Pham Thi Dung        | Lap trinh vien   |
+----+----------------------+------------------+
[B5] Da dong ResultSet, Statement, Connection
```

## Thử nghiệm thêm
- Sửa 1 câu `INSERT` cho trùng `id` (vd 2 câu cùng `id = 1`) → chạy lại → thấy `Loi SQL ... PRIMARY KEY` và `Da rollback giao dich.`, bảng không có nhân viên nào (vì cả giao dịch bị huỷ).
- Bỏ file `.jar` khỏi `-cp` → thấy thông báo `Khong tim thay driver SQLite!`.
