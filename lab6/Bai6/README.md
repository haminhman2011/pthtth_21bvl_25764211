# Bài 6 – Quản Lý Tài Khoản Ngân Hàng

## Đề bài
Xây dựng ứng dụng ngân hàng từ xa, cho phép client **kiểm tra số dư, gửi tiền, rút tiền**.
- Interface có các phương thức:
  - `double getBalance()`
  - `void deposit(double amount)`
  - `boolean withdraw(double amount)`
- Server quản lý tài khoản với số dư ban đầu.
- Client có thể gửi/rút tiền và kiểm tra số dư.

## Các file trong bài
| File | Vai trò |
|------|---------|
| `BankAccount.java` | Interface từ xa với 3 phương thức trên |
| `BankAccountImpl.java` | Server giữ biến `balance` (số dư), xử lý gửi/rút |
| `BankServer.java` | Tạo tài khoản số dư ban đầu **1,000,000 VND**, đăng ký tên `BankService` |
| `BankClient.java` | Menu: xem số dư, gửi tiền, rút tiền |

## Giải thích
- `withdraw()` trả về `false` nếu số tiền rút lớn hơn số dư hoặc số tiền `<= 0`.
- `deposit()` bỏ qua nếu số tiền `<= 0` (client cũng đã kiểm tra trước khi gửi).
- Các hàm ở server có `synchronized` để nếu 2 client cùng rút 1 lúc thì không bị rút lố tiền.
- Mở nhiều client cùng lúc → tất cả dùng chung **1 tài khoản** trên server.
- Hàm `dinhDang()` in tiền dạng `1,000,000 VND` cho dễ đọc.

## Cách chạy
> Mở terminal ở **thư mục `lab6`**.

**Bước 1 – Biên dịch:**
```bash
javac Bai6/*.java
```

**Bước 2 – Terminal 1, chạy server:**
```bash
java Bai6.BankServer
```

**Bước 3 – Terminal 2, chạy client:**
```bash
java Bai6.BankClient
```

## Kết quả mẫu
```
------ MENU ------
1. Xem so du
2. Gui tien
3. Rut tien
0. Thoat
Chon: 1
=> So du hien tai: 1,000,000 VND

Chon: 2
Nhap so tien muon gui: 500000
=> Gui tien thanh cong! So du moi: 1,500,000 VND

Chon: 3
Nhap so tien muon rut: 2000000
=> Rut tien THAT BAI: so du khong du! So du hien tai: 1,500,000 VND

Chon: 3
Nhap so tien muon rut: 300000
=> Rut tien thanh cong! So du moi: 1,200,000 VND
```

## Lỗi thường gặp
- `Connection refused` → chưa chạy server.
- `Port already in use: 1099` → tắt server của bài khác đang chạy (`Ctrl + C`).
- Tắt server là số dư quay về 1,000,000 (vì chỉ lưu trên RAM).
