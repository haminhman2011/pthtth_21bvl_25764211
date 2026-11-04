# Bài 7 – Tính Diện Tích Hình Học

## Đề bài
Client gửi thông số, server tính **diện tích hình chữ nhật, hình tròn, hình tam giác** rồi trả về.
- Interface có các phương thức:
  - `double rectangleArea(double width, double height)`
  - `double circleArea(double radius)`
  - `double triangleArea(double base, double height)`
- Server thực hiện các phép tính.
- Client gửi thông số và nhận diện tích từ server.

## Các file trong bài
| File | Vai trò |
|------|---------|
| `Geometry.java` | Interface từ xa với 3 phương thức tính diện tích |
| `GeometryImpl.java` | Server cài đặt công thức |
| `GeometryServer.java` | Tạo Registry cổng `1099`, đăng ký tên `GeometryService` |
| `GeometryClient.java` | Menu chọn hình, nhập thông số, in kết quả (làm tròn 2 chữ số) |

## Công thức
| Hình | Công thức |
|------|-----------|
| Chữ nhật | `S = width * height` |
| Tròn | `S = Math.PI * r * r` |
| Tam giác | `S = 0.5 * base * height` |

Client chỉ cho nhập số **lớn hơn 0** (độ dài không thể âm hoặc bằng 0).

## Cách chạy
> Mở terminal ở **thư mục `lab6`**.

**Bước 1 – Biên dịch:**
```bash
javac Bai7/*.java
```

**Bước 2 – Terminal 1, chạy server:**
```bash
java Bai7.GeometryServer
```

**Bước 3 – Terminal 2, chạy client:**
```bash
java Bai7.GeometryClient
```

## Kết quả mẫu
```
------ MENU ------
1. Dien tich hinh chu nhat
2. Dien tich hinh tron
3. Dien tich hinh tam giac
0. Thoat
Chon: 1
Nhap chieu dai (width): 5
Nhap chieu rong (height): 4
=> Dien tich hinh chu nhat = 20.00

Chon: 2
Nhap ban kinh: 3
=> Dien tich hinh tron = 28.27

Chon: 3
Nhap canh day: 6
Nhap chieu cao: 4
=> Dien tich tam giac = 12.00
```

## Lỗi thường gặp
- `Connection refused` → chưa chạy server.
- `Port already in use: 1099` → tắt server của bài khác đang chạy (`Ctrl + C`).
- Nhập số thập phân dùng dấu chấm: `2.5` (không dùng dấu phẩy `2,5`).
