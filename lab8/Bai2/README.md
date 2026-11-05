# Bài 2 – Sử dụng giao dịch (Transaction) trong cập nhật dữ liệu

## Đề bài
Thực hiện nhiều câu lệnh `INSERT` / `UPDATE` trong **một giao dịch duy nhất**:
- Tắt auto-commit: `conn.setAutoCommit(false)`.
- Chỉ `conn.commit()` khi **tất cả** thao tác thành công.
- Nếu có lỗi → `conn.rollback()` trong `catch` để huỷ **toàn bộ** thay đổi.

## Tình huống mô phỏng: bán hàng
Một đơn hàng gồm nhiều sản phẩm. Với mỗi sản phẩm phải làm 2 việc:
1. `UPDATE SanPham SET soluong = soluong - ?` (trừ kho)
2. `INSERT INTO ChiTietDonHang ...` (ghi chi tiết đơn)

Nếu không dùng giao dịch, khi sản phẩm thứ 2 bị lỗi thì sản phẩm thứ 1 **đã bị trừ kho** mà đơn hàng lại không tồn tại → dữ liệu bị lệch. Giao dịch đảm bảo "**được cả hoặc không được gì**".

| Tình huống | Diễn biến | Kết quả |
|-----------|-----------|---------|
| 1. Đơn `DH001` (SP01×2, SP02×5) | Đủ hàng | **COMMIT** – kho bị trừ, có 2 dòng chi tiết |
| 2. Đơn `DH002` (SP01×1, SP03×999) | SP01 đã trừ xong, tới SP03 thì không đủ hàng | **ROLLBACK** – SP01 được trả lại như cũ |
| 3. Cập nhật hàng loạt | Tăng giá 10% + nhập kho SP02, rồi `INSERT` trùng khoá chính | **ROLLBACK** – giá và số lượng không đổi |

## File trong bài
| File | Vai trò |
|------|---------|
| `GiaoDichDemo.java` | Tạo bảng `SanPham`, `ChiTietDonHang`, chạy 3 tình huống và in bảng sau mỗi bước |
| `banhang.db` | File CSDL, tự sinh (mỗi lần chạy đều tạo lại từ đầu) |

## Khung code giao dịch
```java
Connection conn = null;
try {
    conn = DriverManager.getConnection(URL);
    conn.setAutoCommit(false);      // 1. bắt đầu giao dịch
    ... nhiều lệnh UPDATE / INSERT ...
    conn.commit();                  // 2. tất cả OK -> lưu
} catch (SQLException e) {
    conn.rollback();                // 3. có lỗi -> huỷ hết
} finally {
    conn.setAutoCommit(true);
    conn.close();                   // 4. luôn đóng kết nối
}
```
Muốn báo lỗi nghiệp vụ (vd: không đủ hàng) thì chủ động `throw new SQLException("...")` để nhảy xuống `catch` → rollback.

## Cách chạy
Mở terminal ở **thư mục `lab8`**:
```bash
javac -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" Bai2/*.java
java --enable-native-access=ALL-UNNAMED -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" Bai2.GiaoDichDemo
```
(Linux/macOS thay `;` bằng `:`.)

## Kết quả mẫu
```
--- TRANG THAI BAN DAU ---
MaSP  | Ten SP             |            Gia | So luong
SP01  | Laptop Dell XPS    |     25,000,000 |       10
SP02  | Chuot Logitech     |        450,000 |       50
SP03  | Ban phim co        |      1,200,000 |       30

>>> Tao don hang DH001: {SP01=2, SP02=5}
    - Da tru kho SP01 x2 (chua commit)
    - Da tru kho SP02 x5 (chua commit)
    => COMMIT: don hang DH001 thanh cong.

--- SAU DON DH001 ---
SP01  | Laptop Dell XPS    |     25,000,000 |        8
SP02  | Chuot Logitech     |        450,000 |       45
SP03  | Ban phim co        |      1,200,000 |       30

>>> Tao don hang DH002: {SP01=1, SP03=999}
    - Da tru kho SP01 x1 (chua commit)
    !! Loi: San pham SP03 (Ban phim co) chi con 30, khong du 999
    => ROLLBACK: huy bo toan bo thay doi trong giao dich.

--- SAU DON DH002 (phai giong het buoc truoc) ---
SP01  | Laptop Dell XPS    |     25,000,000 |        8      <-- van la 8, khong bi tru them
...
>>> Cap nhat hang loat: tang gia 10% + nhap them 100 SP02 + them SP01 (trung ma)
    - Tang gia 3 san pham (chua commit)
    - Nhap kho SP02: 1 dong (chua commit)
    !! Loi: [SQLITE_CONSTRAINT_PRIMARYKEY] A PRIMARY KEY constraint failed ...
    => ROLLBACK: huy bo toan bo thay doi trong giao dich.
...
--- BANG ChiTietDonHang (chi co don DH001 vi DH002 da rollback) ---
#1  DH001  SP01  x2
#2  DH001  SP02  x5
```

## Thử nghiệm thêm
Trong `banHang(...)` xoá dòng `conn.setAutoCommit(false);` rồi chạy lại → ở tình huống 2, SP01 sẽ bị trừ còn **7** dù đơn hàng thất bại (đây chính là lỗi mà giao dịch giúp tránh).
