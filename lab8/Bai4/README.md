# Bài 4 – Sử dụng PreparedStatement để tăng cường bảo mật và hiệu năng

## Đề bài
Dùng `PreparedStatement` (câu SQL có tham số `?`) thay cho `Statement` nối chuỗi, chứng minh:
- **Bảo mật:** chống SQL Injection.
- **Hiệu năng:** câu lệnh được biên dịch 1 lần, chạy nhiều lần, kết hợp `addBatch()`.

## File trong bài
| File | Vai trò |
|------|---------|
| `SanPhamApp.java` | CRUD `SanPham` hoàn toàn bằng `PreparedStatement` + 2 mục demo (7, 8) |
| `sanpham.db` | File CSDL, tự sinh lần chạy đầu, dữ liệu được giữ lại |

## Statement vs PreparedStatement
```java
// Statement – nối chuỗi: dữ liệu người dùng trở thành MỘT PHẦN câu SQL
String sql = "SELECT * FROM SanPham WHERE maSP = '" + ma + "'";
stmt.executeQuery(sql);

// PreparedStatement – tham số ?: dữ liệu chỉ là GIÁ TRỊ, không bao giờ là cú pháp
PreparedStatement ps = conn.prepareStatement("SELECT * FROM SanPham WHERE maSP = ?");
ps.setString(1, ma);
ps.executeQuery();
```
| Hàm gán tham số | Kiểu cột |
|-----------------|----------|
| `setString(i, ...)` | TEXT |
| `setInt(i, ...)` | INTEGER |
| `setDouble(i, ...)` | REAL |

Chỉ số tham số bắt đầu từ **1**. Với `LIKE` thì ghép `%` vào giá trị: `ps.setString(1, "%" + tuKhoa + "%")`.

## Menu
| Mục | Chức năng |
|-----|-----------|
| 1–6 | Liệt kê / tìm / thêm / đổi giá / đổi số lượng / xoá (giống Bài 1, nhưng dùng `?`) |
| 7 | **Demo SQL Injection**: chạy cùng 1 dữ liệu nhập qua `Statement` và `PreparedStatement` |
| 8 | **Demo hiệu năng**: chèn N dòng bằng `Statement` vs `PreparedStatement + addBatch()` |

## Cách chạy
Mở terminal ở **thư mục `lab8`**:
```bash
javac -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" Bai4/*.java
java --enable-native-access=ALL-UNNAMED -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" Bai4.SanPhamApp
```
(Linux/macOS thay `;` bằng `:`.) Muốn reset dữ liệu: xoá file `Bai4/sanpham.db`.

## Kết quả mẫu
**Mục 7 – nhấn Enter để dùng chuỗi tấn công mẫu `SP99' OR '1'='1`:**
```
(a) Statement - cau SQL that su gui di:
    SELECT * FROM SanPham WHERE maSP = 'SP99' OR '1'='1'
| SP01  | Laptop Dell XPS           |     25,000,000 |       10 |
| SP02  | Chuot Logitech            |        450,000 |       50 |
| SP03  | Ban phim co               |      1,200,000 |       30 |
| SP04  | Man hinh LG 24 inch       |      3,500,000 |       15 |
Tong: 4 san pham
    => BI TAN CONG: dieu kien OR '1'='1' luon dung -> lay ra TOAN BO bang!

(b) PreparedStatement - SELECT * FROM SanPham WHERE maSP = ?
Tong: 0 san pham
    => AN TOAN: CSDL chi tim dung ma "SP99' OR '1'='1", khong thuc thi phan chen vao.
```

**Mục 8 – hiệu năng** (chọn `8` → chương trình hỏi `So dong can chen (Enter = 5000):` → nhấn **Enter**; thời gian tuỳ máy):
```
Chen 5,000 dong vao bang tam TestHieuNang (moi cach chay trong 1 giao dich):

(a) Statement - noi chuoi, moi dong la 1 cau SQL MOI:
    INSERT INTO TestHieuNang VALUES (1, 'San pham 1', 1000)
    INSERT INTO TestHieuNang VALUES (2, 'San pham 2', 2000)
    ...
    -> CSDL phai phan tich (parse) cau SQL 5,000 lan
    Thoi gian: 119 ms

(b) PreparedStatement + addBatch() - 1 cau SQL co tham so:
    INSERT INTO TestHieuNang VALUES (?, ?, ?)
    -> bien dich 1 lan, moi dong chi setInt/setString/setDouble roi addBatch()
    -> cu 1000 dong goi executeBatch() 1 lan
    Thoi gian: 41 ms

=> PreparedStatement nhanh hon khoang 2.9 lan.
   (Bang tam TestHieuNang da duoc xoa, khong anh huong bang SanPham.)
```
`Statement` bắt CSDL phân tích lại câu SQL **N lần**; `PreparedStatement` chỉ biên dịch **1 lần**, các lần sau chỉ thay giá trị tham số, `addBatch()` gom nhiều lệnh gửi 1 lượt.

**Mục 3 – tên có dấu nháy đơn vẫn thêm được** (ở Bài 1 sẽ lỗi cú pháp):
```
Ma SP: SP05
Ten SP: Chuot O'Neil
...
Da them 1 san pham.
```
