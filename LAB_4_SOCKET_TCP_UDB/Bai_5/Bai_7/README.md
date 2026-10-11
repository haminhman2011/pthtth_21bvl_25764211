# Bài 7 — Server lưu nhật ký tin nhắn

Biên dịch tại thư mục gốc dự án:

```powershell
javac -encoding UTF-8 -d . Bai_5/Bai_7/*.java
```

Chạy server trong một terminal:

```powershell
java Bai_5.Bai_7.LogServer
```

Mở terminal khác chạy client (có thể mở nhiều client):

```powershell
java Bai_5.Bai_7.LogClient
```

Nhập:

```text
HELLO client-01_A
Xin chào server
HET vẫn là nội dung bình thường
QUIT
```

Server lưu tại `Bai_5/Bai_7/data/logs/client-01_A.txt`. Thư mục tự tạo khi chạy server; dữ liệu được ghi nối tiếp, không ghi đè khi kết nối lại hoặc khởi động lại server.

Mặc định `localhost:5007`. Server nhận `[port] [thư_mục_log]`; client nhận `[host] [port]`. Ví dụ:

```powershell
java Bai_5.Bai_7.LogServer 6007 Bai_5/Bai_7/data/logs
java Bai_5.Bai_7.LogClient localhost 6007
```

## Giao thức TCP

Mã hóa UTF-8, mỗi thông điệp và mỗi phản hồi là một dòng.

| Yêu cầu | Phản hồi | Xử lý |
| --- | --- | --- |
| `HELLO client-01_A` khi chưa đăng ký | `OK HELLO client-01_A` | Gắn ID cho kết nối |
| Nội dung trước HELLO | `ERR HELLO_REQUIRED` | Không ghi file |
| ID không hợp lệ | `ERR INVALID_CLIENT_ID` | Cho phép nhập lại HELLO |
| Nội dung sau HELLO | `OK LOGGED` | Chỉ trả OK sau khi ghi và đóng file thành công |
| Không ghi được file | `ERR LOG_WRITE` | Không báo lưu thành công; tiếp tục phiên |
| `QUIT` | `BYE` | Đóng kết nối, không ghi lệnh QUIT |

ClientId chỉ chấp nhận chữ ASCII `A-Z`, `a-z`, số `0-9`, `-`, `_`, ít nhất một ký tự. Không tự bỏ khoảng trắng trong ID; dấu chấm, dấu phân cách đường dẫn và khoảng trắng đều bị từ chối. ID được giữ nguyên trong tên file; trên Windows, tên khác nhau chỉ bởi hoa/thường thường trỏ đến cùng file. ID hợp lệ theo cú pháp nhưng không dùng được làm tên file trên hệ điều hành sẽ báo `ERR LOG_WRITE` khi ghi.

Sau HELLO, mọi dòng trừ **đúng `QUIT`** là nội dung, bao gồm dòng rỗng, `HET`, chuỗi có `HET`, hoặc chuỗi có `QUIT` ở giữa. HELLO gửi lại sau đăng ký cũng là nội dung, không đổi ID. QUIT có thể kết thúc phiên cả trước khi đăng ký. Lệnh phân biệt hoa/thường.

## Cấu trúc nhật ký và đồng thời

Mỗi dòng có ba cột phân cách bằng tab:

```text
timestamp<TAB>remote_address:port<TAB>content
```

Timestamp là ISO-8601 có offset, theo đồng hồ và múi giờ server. Nội dung giữ UTF-8; dấu `\` được escape thành `\\`, tab thành `\t` để mỗi dòng luôn có ba cột. Thời điểm ghi và địa chỉ remote lấy từ server, không tin dữ liệu do client tự khai báo.

Server dùng thread pool 32 worker và semaphore giới hạn 32 phiên; vượt giới hạn trả `ERR SERVER_FULL`. Trước HELLO, timeout đọc là 60 giây; sau HELLO không giới hạn thời gian đọc. Khi client ngắt, socket được đóng và slot được giải phóng; server vẫn phục vụ các client khác.

Nhiều kết nối có thể dùng cùng clientId. Các thao tác ghi cùng file được khóa trong server để tránh xen dòng hoặc mất bản ghi. Chạy một server cho mỗi thư mục log; cơ chế khóa này không phối hợp giữa nhiều tiến trình server. Đóng writer bảo đảm flush đến hệ điều hành, không cam kết chống mất dữ liệu khi máy mất điện.

## Minh chứng

`minh_chung.txt` lưu kết quả kiểm tra thực tế: ID sai, nội dung trước HELLO, UTF-8, HET, QUIT, timestamp/remote, ghi đồng thời 200 dòng cùng ID, kết nối lại ghi nối tiếp, ngắt đột ngột và lỗi ghi file.

Kiểm tra lại sau khi biên dịch:

```powershell
python Bai_5/Bai_7/verify.py
```

Bài kiểm tra dùng thư mục log tạm riêng, không sửa nhật ký người dùng.
