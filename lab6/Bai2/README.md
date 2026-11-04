# Bài 2 – Tính Tổng Hai Số

## Đề bài
Client gửi hai số nguyên `a` và `b` đến server, server trả về tổng `a + b`.
- Interface chứa phương thức `int add(int a, int b)`.
- Server triển khai phương thức này.
- Client nhập hai số từ bàn phím và gửi đến server để nhận kết quả.

## Các file trong bài
| File | Vai trò |
|------|---------|
| `Calculator.java` | Interface từ xa, có `int add(int a, int b)` |
| `CalculatorImpl.java` | Server cài đặt phép cộng |
| `CalculatorServer.java` | Tạo Registry cổng `1099`, đăng ký tên `CalculatorService` |
| `CalculatorClient.java` | Nhập a, b bằng `Scanner`, gọi `add()` từ xa, in kết quả |

## Giải thích
- Client dùng hàm `nhapSoNguyen()` để nhập; nếu gõ chữ thì báo lỗi và bắt nhập lại.
- Tham số `a`, `b` được RMI tự đóng gói gửi qua mạng, server cộng xong trả kết quả về.
- Sau mỗi lần tính, client hỏi có muốn tính tiếp không (`c` = có, `k` = không).

## Cách chạy
> Mở terminal ở **thư mục `lab6`** (thư mục cha của `Bai2`).

**Bước 1 – Biên dịch:**
```bash
javac Bai2/*.java
```

**Bước 2 – Terminal 1, chạy server:**
```bash
java Bai2.CalculatorServer
```

**Bước 3 – Terminal 2, chạy client:**
```bash
java Bai2.CalculatorClient
```
(Server ở máy khác: `java Bai2.CalculatorClient <IP_server>`)

## Kết quả mẫu
Client:
```
===== CALCULATOR CLIENT =====
Da ket noi toi server localhost
Nhap so a: 5
Nhap so b: abc
  Ban phai nhap so nguyen! Nhap lai.
Nhap so b: 7
=> Ket qua tu server: 5 + 7 = 12
Ban co muon tinh tiep khong? (c/k): k
Tam biet!
```
Server:
```
[Server] Client yeu cau: 5 + 7 = 12
```

## Lỗi thường gặp
- `Connection refused` → chưa chạy server.
- `Port already in use: 1099` → tắt server của bài khác đang chạy (`Ctrl + C`).
