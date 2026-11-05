# Bài 5 – Truy vấn dữ liệu nâng cao (Aggregation Functions)

## Đề bài
Thêm nhiều bản ghi vào bảng `NhanVien` có cột lương (`luong`), rồi dùng JDBC truy vấn:
- Tính tổng lương `SUM(luong)`.
- Tìm mức lương trung bình `AVG(luong)`.
- Tìm nhân viên có lương cao nhất / thấp nhất `MAX / MIN(luong)`.
- Đếm số lượng nhân viên theo chức vụ (`COUNT` và `GROUP BY`).
- *(Mở rộng)* Lọc nhóm bằng `HAVING`.

## File trong bài
| File | Vai trò |
|------|---------|
| `ThongKeLuong.java` | Tạo bảng, thêm 12 nhân viên (PreparedStatement + batch, 1 giao dịch), in 5 thống kê |
| `nhanvien.db` | File CSDL, tự sinh (mỗi lần chạy tạo lại từ đầu) |

## Các câu SQL dùng trong bài
```sql
-- 1 + 2. Tổng, trung bình, số nhân viên
SELECT COUNT(*) AS so_nv, SUM(luong) AS tong, AVG(luong) AS tb FROM NhanVien;

-- 3. Nhân viên lương cao nhất / thấp nhất (dùng truy vấn con để lấy được cả tên,
--    và nếu nhiều người cùng mức lương thì lấy ra hết)
SELECT * FROM NhanVien WHERE luong = (SELECT MAX(luong) FROM NhanVien);
SELECT * FROM NhanVien WHERE luong = (SELECT MIN(luong) FROM NhanVien);

-- 4. Đếm theo chức vụ
SELECT chuc_vu, COUNT(*) AS so_luong, SUM(luong) AS tong, AVG(luong) AS tb
FROM NhanVien GROUP BY chuc_vu ORDER BY so_luong DESC;

-- 5. (Mở rộng) Chức vụ có lương TB > ngưỡng
SELECT chuc_vu, COUNT(*), AVG(luong) AS tb FROM NhanVien
GROUP BY chuc_vu HAVING AVG(luong) > ? ORDER BY tb DESC;
```
- Đặt **bí danh** (`AS tong`) để lấy kết quả dễ đọc: `rs.getDouble("tong")`. Cũng có thể lấy theo số thứ tự cột: `rs.getDouble(2)`.
- `WHERE` lọc **từng dòng** trước khi nhóm, `HAVING` lọc **từng nhóm** sau khi `GROUP BY`.

## Cách chạy
Mở terminal ở **thư mục `lab8`**:
```bash
javac -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" Bai5/*.java
java --enable-native-access=ALL-UNNAMED -cp ".;lib/sqlite-jdbc-3.53.4.0.jar" Bai5.ThongKeLuong
```
(Linux/macOS thay `;` bằng `:`.)

## Kết quả mẫu
```
===== 1. TONG LUONG - SUM(luong)  &  2. LUONG TRUNG BINH - AVG(luong) =====
So nhan vien      : 12
Tong luong        : 260,500,000 VND
Luong trung binh  : 21,708,333 VND

===== 3. NHAN VIEN LUONG CAO NHAT / THAP NHAT - MAX / MIN(luong) =====
  Cao nhat : #1 Nguyen Van An (Giam doc) - 45,000,000 VND
  Thap nhat: #10 Ngo Thi Nga (Nhan su) - 11,000,000 VND

===== 4. SO LUONG NHAN VIEN THEO CHUC VU - COUNT + GROUP BY =====
Chuc vu         | So luong |     Tong luong |       Luong TB
----------------+----------+----------------+---------------
Lap trinh vien  |        4 |     87,500,000 |     21,875,000
Ke toan         |        2 |     29,500,000 |     14,750,000
Tester          |        2 |     25,500,000 |     12,750,000
Truong phong    |        2 |     62,000,000 |     31,000,000
Giam doc        |        1 |     45,000,000 |     45,000,000
Nhan su         |        1 |     11,000,000 |     11,000,000

===== 5. (MO RONG) CHUC VU CO LUONG TB > 15,000,000 - GROUP BY + HAVING =====
  Giam doc        : 1 nguoi, TB 45,000,000 VND
  Truong phong    : 2 nguoi, TB 31,000,000 VND
  Lap trinh vien  : 4 nguoi, TB 21,875,000 VND
```
