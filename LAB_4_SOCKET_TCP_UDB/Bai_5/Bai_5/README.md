# Bài 5 — Chat TCP nhiều client

Biên dịch từ thư mục gốc dự án:

```powershell
javac -encoding UTF-8 -d . Bai_5/Bai_5/*.java
```

Chạy server:

```powershell
java Bai_5.Bai_5.ChatServer
```

Mở nhiều terminal, mỗi terminal chạy một client:

```powershell
java Bai_5.Bai_5.ChatClient
```

Mặc định `localhost:5005`. Server nhận `[port]`; client nhận `[host] [port]`. Nhấn Ctrl+C để dừng server hoặc đóng đột ngột một client.

## Giao thức UTF-8, mỗi thông điệp một dòng

Server gửi `NICK_REQUIRED` sau khi kết nối. Client phải đăng ký nickname trước khi chat; nickname gồm 1–20 ký tự chữ ASCII, chữ số hoặc `_`, phân biệt hoa/thường. Nickname không đổi trong phiên. Nếu bị trùng, client được chọn lại mà không cần kết nối lại.

| Yêu cầu | Phản hồi cho người gửi | Hành vi |
| --- | --- | --- |
| `NICK alice` | `OK NICK alice` | Đăng ký nickname duy nhất |
| `NICK alice` khi đã có người dùng | `ERR NICK_IN_USE` | Chọn nickname khác |
| `USERS` | `USERS alice bob` | Danh sách nickname, sắp xếp tăng dần |
| `MSG Xin chào` | `OK MSG` | Các client khác nhận `MSG alice Xin chào` |
| `MSG ` hoặc nội dung chỉ có khoảng trắng | `ERR EMPTY_MESSAGE` | Không broadcast |
| `QUIT` | `BYE` | Đóng kết nối, xóa nickname |

Lệnh phân biệt hoa/thường. Nickname sai trả `ERR INVALID_NICK`; lệnh trước khi đăng ký trả `ERR NICK_REQUIRED`; lệnh không hợp lệ sau khi đăng ký trả `ERR UNKNOWN_COMMAND`. `QUIT` dùng được cả trước khi đăng ký. Danh sách USERS là ảnh chụp tại thời điểm xử lý và có thể thay đổi khi các client khác đăng nhập hoặc thoát.

## Quản lý đồng thời và ngắt kết nối

Server dùng thread pool cố định 32 worker, tối đa 32 kết nối đang phục vụ, kể cả client chưa đăng ký. Kết nối vượt giới hạn nhận `ERR SERVER_FULL` rồi đóng. Semaphore giới hạn số phiên trước khi đưa vào pool. Client chưa đăng ký có thời gian chờ dữ liệu 60 giây; client đã đăng ký không có timeout đọc.

Danh sách người dùng dùng `ConcurrentHashMap`; `putIfAbsent` đăng ký nickname nguyên tử để hai client không chiếm cùng tên. Mỗi lần ghi phản hồi/broadcast được đồng bộ theo client nhận để các dòng không xen lẫn. Client có thread nhập riêng nên tin nhắn đến hiển thị cả khi đang chờ bàn phím.

Server xóa người dùng và đóng socket trong `finally` khi nhận QUIT, EOF hoặc lỗi I/O. Khi broadcast gặp socket hỏng, server cũng loại người nhận đó; các phiên khác tiếp tục hoạt động. Trường hợp mạng mất mà chưa có EOF/lỗi TCP có thể cần thời gian hệ điều hành phát hiện. Đây là bài thực hành dùng ghi socket đồng bộ; client không đọc dữ liệu lâu có thể làm chậm luồng broadcast đến nó.

## Minh chứng

Mở ba client, đăng ký `alice`, `bob`, `carol`; thử nickname trùng, gửi `USERS`, gửi `MSG Xin chào`, đóng đột ngột client bob rồi gửi `USERS` và tiếp tục chat. Nickname bob có thể được đăng ký lại sau khi server loại phiên cũ.

`minh_chung.txt` chứa kết quả chạy thực tế, gồm broadcast UTF-8, gửi đồng thời, nickname trùng đồng thời, ngắt không gửi QUIT, tái sử dụng nickname và phiên ChatClient. Kiểm tra lại sau khi biên dịch:

```powershell
python Bai_5/Bai_5/verify.py
```
