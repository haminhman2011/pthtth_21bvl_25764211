# Bài 3 – Kiểm Tra Số Nguyên Tố

## Đề bài
Client nhập một số, server kiểm tra có phải **số nguyên tố** hay không rồi trả kết quả.
- Interface chứa phương thức `boolean isPrime(int n)`.
- Server kiểm tra số nguyên tố và trả kết quả.
- Client nhập số và nhận kết quả từ server.

## Các file trong bài
| File | Vai trò |
|------|---------|
| `PrimeChecker.java` | Interface từ xa, có `boolean isPrime(int n)` |
| `PrimeCheckerImpl.java` | Server cài đặt thuật toán kiểm tra số nguyên tố |
| `PrimeServer.java` | Tạo Registry cổng `1099`, đăng ký tên `PrimeService` |
| `PrimeClient.java` | Nhập số liên tục, gửi lên server, gõ `q` để thoát |

## Thuật toán kiểm tra (ở server)
- `n < 2` → không phải số nguyên tố (0, 1, số âm).
- `n == 2` → là số nguyên tố.
- `n` chẵn → không phải.
- Thử chia `n` cho các số lẻ `i = 3, 5, 7, ...` với `i * i <= n`. Nếu chia hết → không phải.
- Chỉ cần thử tới căn bậc hai của `n` cho nhanh. Dùng kiểu `long` cho `i` để `i * i` không bị tràn số.

## Cách chạy
> Mở terminal ở **thư mục `lab6`**.

**Bước 1 – Biên dịch:**
```bash
javac Bai3/*.java
```

**Bước 2 – Terminal 1, chạy server:**
```bash
java Bai3.PrimeServer
```

**Bước 3 – Terminal 2, chạy client:**
```bash
java Bai3.PrimeClient
```

## Kết quả mẫu
```
===== PRIME CLIENT =====
Nhap so can kiem tra (go 'q' de thoat)
Nhap so: 7
=> 7 LA so nguyen to
Nhap so: 10
=> 10 KHONG PHAI so nguyen to
Nhap so: 1
=> 1 KHONG PHAI so nguyen to
Nhap so: q
Tam biet!
```

## Lỗi thường gặp
- `Connection refused` → chưa chạy server.
- `Port already in use: 1099` → tắt server của bài khác đang chạy (`Ctrl + C`).
