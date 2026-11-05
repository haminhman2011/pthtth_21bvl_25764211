# Bài 3 – Xử lý ngoại lệ (Exception Handling) triệt để

## Đề bài
- Thực hiện lại **Bài 1** (CRUD `SanPham`) và áp dụng khối **try-catch-finally** một cách nghiêm ngặt.
- Đảm bảo mọi tài nguyên JDBC (`Connection`, `Statement`, `ResultSet`) **luôn được đóng trong `finally`** để tránh rò rỉ tài nguyên, kể cả khi có `SQLException`.

## File trong bài
| File | Vai trò |
|------|---------|
| `DbUtil.java` | Nạp driver, mở kết nối, `close(rs, stmt, conn)` và `rollback(conn)` an toàn |
| `SanPhamApp.java` | Menu CRUD giống Bài 1 + mục **7** để cố tình gây lỗi và kiểm chứng `finally` |
| `sanpham.db` | File CSDL, tự sinh lần chạy đầu, dữ liệu được giữ lại |

## Quy tắc áp dụng cho MỌI hàm truy cập CSDL
```java
Connection conn = null;          // 1. khai báo NGOÀI try (để finally nhìn thấy)
Statement  stmt = null;
ResultSet  rs   = null;
try {
    conn = DbUtil.getConnection();   // 2. mọi thao tác JDBC nằm trong try
    stmt = conn.createStatement();
    rs   = stmt.executeQuery(sql);
    ...
} catch (SQLException e) {
    System.err.println("Loi: " + e.getMessage());   // 3. bắt và in lỗi
    DbUtil.rollback(conn);                         //    (nếu đang trong giao dịch)
} finally {
    DbUtil.close(rs, stmt, conn);   // 4. LUÔN đóng, kể cả khi lỗi
}
```

Trong `DbUtil.close(...)` **mỗi tài nguyên có một try-catch riêng** và kiểm tra `!= null`:
```java
if (rs != null)   try { rs.close();   } catch (SQLException e) { ... }
if (stmt != null) try { stmt.close(); } catch (SQLException e) { ... }
if (conn != null) try { conn.close(); } catch (SQLException e) { ... }
```
→ Nếu đóng `rs` bị lỗi thì vẫn tiếp tục đóng `stmt` và `conn`. Kiểm tra `null` vì lỗi có thể xảy ra **trước khi** tài nguyên được tạo (vd lỗi ngay ở `getConnection`, thì `stmt`, `rs` vẫn là `null`).

### So sánh với Bài 1
| | Bài 1 | Bài 3 |
|--|-------|-------|
| Cách đóng tài nguyên | try-with-resources (Java tự đóng) | `finally` viết tay, đóng từng cái |
| Lỗi SQL | ném lên `main` xử lý | bắt ngay trong từng hàm |
| Thêm dữ liệu mẫu | từng lệnh riêng | trong 1 giao dịch, lỗi thì rollback |

## Cách chạy
Mở terminal ở **thư mục `lab8`**:
```bash
javac -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" Bai3/*.java
java --enable-native-access=ALL-UNNAMED -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" Bai3.SanPhamApp
```
(Linux/macOS thay `;` bằng `:`.) Muốn reset dữ liệu: xoá file `Bai3/sanpham.db`.

## Kết quả mẫu – mục 7 (gây lỗi SQL)
```
Chon: 7
Thuc thi: SELECT * FROM BangKhongTonTai
[catch] Bat duoc loi: [SQLITE_ERROR] SQL error or missing database (no such table: BangKhongTonTai)
   [finally] da dong Statement
   [finally] da dong Connection
   Kiem tra: stmt.isClosed() = true, conn.isClosed() = true
```
- Dòng `(dong nay se khong duoc in)` không xuất hiện → lỗi nhảy thẳng vào `catch`.
- `rs` vẫn là `null` (chưa kịp tạo) nên không cần đóng; `stmt` và `conn` vẫn được `finally` đóng → **không rò rỉ**.

Các mục 1–6 hoạt động giống [Bài 1](../Bai1/README.md); thử thêm trùng mã (`SP01`) hoặc nhập giá âm sẽ thấy lỗi được bắt và chương trình vẫn chạy tiếp.
