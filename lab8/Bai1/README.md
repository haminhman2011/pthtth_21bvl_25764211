# Bài 1 – Quản lý danh mục sản phẩm (CRUD với Statement)

## Đề bài
Mở rộng bài NhanVien thành hệ thống quản lý bảng `SanPham(maSP, tenSP, gia, soluong)`:
- **Create:** thêm mới (ít nhất 3 sản phẩm).
- **Read:** liệt kê tất cả sản phẩm, tìm kiếm theo `tenSP`.
- **Update:** thay đổi giá hoặc số lượng theo `maSP`.
- **Delete:** xoá sản phẩm theo `maSP`.

## File trong bài
| File | Vai trò |
|------|---------|
| `SanPhamApp.java` | Tạo bảng, thêm 4 sản phẩm mẫu (nếu bảng trống), menu CRUD |
| `sanpham.db` | File CSDL, **tự sinh** lần chạy đầu, dữ liệu được **giữ lại** giữa các lần chạy |

## Cấu trúc bảng
```sql
CREATE TABLE IF NOT EXISTS SanPham (
    maSP    TEXT PRIMARY KEY,
    tenSP   TEXT NOT NULL,
    gia     REAL NOT NULL CHECK (gia >= 0),
    soluong INTEGER NOT NULL CHECK (soluong >= 0)
);
```

## Các câu SQL tương ứng từng chức năng
| Menu | Chức năng | SQL |
|------|-----------|-----|
| 1 | Liệt kê | `SELECT * FROM SanPham ORDER BY maSP` |
| 2 | Tìm theo tên | `SELECT * FROM SanPham WHERE tenSP LIKE '%tukhoa%'` |
| 3 | Thêm | `INSERT INTO SanPham (maSP, tenSP, gia, soluong) VALUES (...)` |
| 4 | Đổi giá | `UPDATE SanPham SET gia = ... WHERE maSP = '...'` |
| 5 | Đổi số lượng | `UPDATE SanPham SET soluong = ... WHERE maSP = '...'` |
| 6 | Xoá | `DELETE FROM SanPham WHERE maSP = '...'` |

- `executeQuery()` dùng cho SELECT, `executeUpdate()` cho INSERT/UPDATE/DELETE và trả về **số dòng bị ảnh hưởng** → nếu = 0 thì báo "Khong tim thay san pham".
- Tài nguyên được đóng tự động bằng **try-with-resources** `try (Connection conn = ...; Statement stmt = ...) { }`.
- Bài này cố ý **nối chuỗi** để ghép SQL (cách cơ bản của `Statement`). Nhược điểm: nhập tên có dấu `'` sẽ lỗi cú pháp và dễ bị SQL Injection → được khắc phục ở [Bài 4](../Bai4/README.md).

## Cách chạy
Mở terminal ở **thư mục `lab8`**:
```bash
javac -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" Bai1/*.java
java --enable-native-access=ALL-UNNAMED -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" Bai1.SanPhamApp
```
(Linux/macOS thay `;` bằng `:`.) Muốn reset dữ liệu: xoá file `Bai1/sanpham.db`.

## Kết quả mẫu
```
Da them 4 san pham mau vao bang SanPham.

===== QUAN LY SAN PHAM (Statement) =====
1. Liet ke tat ca san pham
2. Tim san pham theo ten
3. Them san pham
4. Cap nhat gia
5. Cap nhat so luong
6. Xoa san pham
0. Thoat
Chon: 1
+-------+---------------------------+----------------+----------+
| MaSP  | Ten san pham              |            Gia | So luong |
+-------+---------------------------+----------------+----------+
| SP01  | Laptop Dell XPS           |     25,000,000 |       10 |
| SP02  | Chuot Logitech            |        450,000 |       50 |
| SP03  | Ban phim co               |      1,200,000 |       30 |
| SP04  | Man hinh LG 24 inch       |      3,500,000 |       15 |
+-------+---------------------------+----------------+----------+
Tong: 4 san pham

Chon: 4
Ma SP can doi gia: SP01
Gia moi: 23000000
Da cap nhat gia cho SP01

Chon: 6
Ma SP can xoa: SP99
Khong tim thay san pham SP99

Chon: 3
Ma SP: SP01
Ten SP: Trung
Gia: 1
So luong: 1
Loi SQL: [SQLITE_CONSTRAINT_PRIMARYKEY] A PRIMARY KEY constraint failed (UNIQUE constraint failed: SanPham.maSP)
```

## Kịch bản test gợi ý
1. Chọn `1` xem 4 sản phẩm mẫu.
2. Chọn `3` thêm `SP05 / Tai nghe Sony / 2000000 / 20`.
3. Chọn `4` đổi giá `SP01`, chọn `5` đổi số lượng `SP02`.
4. Chọn `2` tìm `laptop` (LIKE không phân biệt hoa thường với chữ không dấu).
5. Chọn `6` xoá `SP03`, rồi chọn `1` kiểm tra lại.
