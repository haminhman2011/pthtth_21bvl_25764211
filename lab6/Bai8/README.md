# Bài 8 – Hệ Thống Đặt Vé Máy Bay

## Đề bài
Viết ứng dụng RMI quản lý **đặt chỗ vé máy bay từ xa**.
- Interface có các phương thức:
  - `boolean bookTicket(String flightNumber, int seats)`
  - `int availableSeats(String flightNumber)`
  - `boolean cancelBooking(String flightNumber, int seats)`
- Server quản lý danh sách các chuyến bay.
- Client có thể đặt, hủy vé và kiểm tra số ghế trống.

## Các file trong bài
| File | Vai trò |
|------|---------|
| `FlightBooking.java` | Interface từ xa (3 hàm theo đề + `listFlights()` bổ sung để xem danh sách) |
| `Flight.java` | Lớp chuyến bay: mã, tuyến, tổng ghế, số ghế đã đặt |
| `FlightBookingImpl.java` | Server lưu `Map<String, Flight>`, xử lý đặt/hủy/kiểm tra |
| `FlightServer.java` | Tạo Registry cổng `1099`, đăng ký tên `FlightService` |
| `FlightClient.java` | Menu: xem danh sách, kiểm tra ghế, đặt vé, hủy vé |

## Dữ liệu có sẵn ở server
| Mã | Tuyến | Tổng ghế |
|----|-------|----------|
| VN123 | Ha Noi - TP.HCM | 100 |
| VJ456 | TP.HCM - Da Nang | 50 |
| QH789 | Ha Noi - Phu Quoc | 30 |

## Quy tắc xử lý
- **Đặt vé**: thất bại nếu sai mã chuyến bay hoặc số ghế muốn đặt > số ghế còn trống.
- **Hủy vé**: thất bại nếu sai mã hoặc hủy nhiều hơn số ghế đã đặt.
- **Kiểm tra ghế trống**: trả về `-1` nếu không có chuyến bay đó.
- Mã chuyến bay không phân biệt hoa thường (`vn123` = `VN123`).
- Các hàm ở server có `synchronized` để 2 client đặt cùng lúc không bị đặt lố số ghế.

## Cách chạy
> Mở terminal ở **thư mục `lab6`**.

**Bước 1 – Biên dịch:**
```bash
javac Bai8/*.java
```

**Bước 2 – Terminal 1, chạy server:**
```bash
java Bai8.FlightServer
```

**Bước 3 – Terminal 2, chạy client:**
```bash
java Bai8.FlightClient
```

## Kết quả mẫu
```
Chon: 1
=== DANH SACH CHUYEN BAY ===
VN123  | Ha Noi - TP.HCM      | Tong: 100 | Da dat:   0 | Con trong: 100
VJ456  | TP.HCM - Da Nang     | Tong:  50 | Da dat:   0 | Con trong:  50
QH789  | Ha Noi - Phu Quoc    | Tong:  30 | Da dat:   0 | Con trong:  30

Chon: 3
Nhap ma chuyen bay (vd: VN123): qh789
Nhap so ghe: 5
=> Dat 5 ghe thanh cong! Con lai: 25 ghe

Chon: 3
Nhap ma chuyen bay (vd: VN123): QH789
Nhap so ghe: 100
=> Dat ve THAT BAI (sai ma chuyen bay hoac khong du ghe trong)

Chon: 4
Nhap ma chuyen bay (vd: VN123): QH789
Nhap so ghe: 2
=> Huy 2 ghe thanh cong! Con lai: 27 ghe
```

## Lỗi thường gặp
- `Connection refused` → chưa chạy server.
- `Port already in use: 1099` → tắt server của bài khác đang chạy (`Ctrl + C`).
