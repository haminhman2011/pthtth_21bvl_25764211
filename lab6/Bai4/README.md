# Bài 4 – Quản Lý Danh Bạ

## Đề bài
Xây dựng hệ thống quản lý danh bạ từ xa bằng RMI. Client có thể **thêm, sửa, xóa, tìm kiếm** liên hệ trong danh bạ lưu trên server.
- Interface có các phương thức:
  - `void addContact(String name, String phone)`
  - `String findContact(String name)`
  - `boolean deleteContact(String name)`
- Server lưu danh bạ bằng `HashMap<String, String>`.
- Client nhập dữ liệu và gửi yêu cầu.

## Các file trong bài
| File | Vai trò |
|------|---------|
| `PhoneBook.java` | Interface từ xa (3 hàm theo đề + 2 hàm bổ sung `updateContact`, `getAllContacts`) |
| `PhoneBookImpl.java` | Server lưu danh bạ bằng `HashMap<String, String>` (key = tên, value = số ĐT) |
| `PhoneBookServer.java` | Tạo Registry cổng `1099`, đăng ký tên `PhoneBookService` |
| `PhoneBookClient.java` | Menu cho người dùng chọn chức năng |

## Giải thích
- Đề có yêu cầu "sửa" nhưng interface chỉ cho 3 hàm, nên em thêm hàm `updateContact()` để sửa, và `getAllContacts()` để xem hết danh bạ.
- `findContact()` trả về `null` nếu không tìm thấy → client kiểm tra `null` để báo.
- Các hàm ở server có `synchronized` vì nhiều client có thể gọi cùng lúc, mà `HashMap` không an toàn khi nhiều luồng cùng sửa.
- Danh bạ lưu trên RAM của server: **tắt server là mất dữ liệu**. Server có sẵn 2 liên hệ mẫu: `An`, `Binh`.
- Tên phân biệt hoa thường (`An` khác `an`).
- Có thể mở nhiều client cùng lúc, tất cả dùng chung 1 danh bạ trên server.

## Cách chạy
> Mở terminal ở **thư mục `lab6`**.

**Bước 1 – Biên dịch:**
```bash
javac Bai4/*.java
```

**Bước 2 – Terminal 1, chạy server:**
```bash
java Bai4.PhoneBookServer
```

**Bước 3 – Terminal 2, chạy client:**
```bash
java Bai4.PhoneBookClient
```

## Kết quả mẫu
```
------ MENU ------
1. Them lien he
2. Tim lien he
3. Sua so dien thoai
4. Xoa lien he
5. Xem toan bo danh ba
0. Thoat
Chon: 1
Nhap ten: Man
Nhap so dien thoai: 0987654321
=> Da them lien he Man

Chon: 2
Nhap ten can tim: Man
=> Man: 0987654321

Chon: 5
=== DANH BA ===
1. Binh - 0902222222
2. An - 0901111111
3. Man - 0987654321
```
> Thứ tự in ra có thể khác vì `HashMap` không giữ thứ tự thêm vào.

## Lỗi thường gặp
- `Connection refused` → chưa chạy server.
- `Port already in use: 1099` → tắt server của bài khác đang chạy (`Ctrl + C`).
- Nhập tên tiếng Việt có dấu bị lỗi font → gõ `chcp 65001` trong terminal trước khi chạy, hoặc nhập không dấu.
