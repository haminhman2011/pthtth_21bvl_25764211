# Bài 8 — Truyền file TCP an toàn

Tại thư mục gốc dự án, biên dịch:

```powershell
javac -encoding UTF-8 -d . Bai_5/Bai_8/*.java
```

Terminal server:

```powershell
java Bai_5.Bai_8.FileServer
```

Terminal client (thay đường dẫn bằng file cần gửi):

```powershell
java Bai_5.Bai_8.FileClient "C:\Users\Public\Documents\van-ban.txt"
```

Mặc định `localhost:5008`; server nhận `[port]`, client nhận `<file> [host] [port]`. Ví dụ:

```powershell
java Bai_5.Bai_8.FileServer 6008
java Bai_5.Bai_8.FileClient "D:\anh.png" localhost 6008
```

File chỉ được lưu vào thư mục cố định `Bai_5/Bai_8/upload`, tính từ thư mục chạy server. Server tự tạo thư mục; không nhận tham số thư mục từ client. Chạy từ thư mục gốc để có đúng vị trí trên. Server phục vụ lần lượt các kết nối; mỗi kết nối truyền một file.

## Giao thức nhị phân

Dùng `DataInputStream`/`DataOutputStream`, số nguyên big-endian. Chuỗi được mã hóa bằng **int số byte UTF-8 + các byte UTF-8**, không dùng `writeUTF`.

1. Client gửi metadata: tên file (chuỗi), kích thước (Java `long`, 8 byte), SHA-256 (chuỗi 64 ký tự hex).
2. Server kiểm tra metadata, tên file và file đã tồn tại. Hợp lệ thì trả chuỗi `READY`; không hợp lệ trả `ERR mã_lỗi` và đóng kết nối.
3. Sau READY, client gửi đúng số byte đã khai báo, không cần gửi EOF.
4. Server đọc đủ số byte vào file tạm trong upload, đóng file, **đọc lại file đã lưu để tính SHA-256**. Hash đúng thì chuyển sang tên đích, trả `OK`; hash sai trả `ERR HASH_MISMATCH` và xóa file tạm.
5. Server đóng kết nối. Không dùng EOF làm ranh giới duy nhất, kể cả file rỗng.

Giới hạn thực hành: file tối đa 1 GiB (vẫn truyền kích thước bằng long); tên tối đa 1.024 byte UTF-8. Read/connect timeout của client: 10 giây/3 giây; read timeout server: 10 giây mỗi lần đọc. Timeout đọc là thời gian chờ dữ liệu, không phải giới hạn tổng thời gian upload. Dữ liệu được truyền theo buffer 8 KiB, không nạp cả file vào RAM.

## Kiểm tra tên và lỗi

Client thông thường chỉ gửi `getFileName()`, không gửi đường dẫn nguồn. Server vẫn tự kiểm tra độc lập: từ chối tên rỗng, `.`, `..`, dấu `/`, `\`, dấu `:`, ký tự điều khiển, ký tự không hợp lệ trên Windows và tên thiết bị dành riêng. Đường dẫn `../file.txt`, `..\file.txt`, đường dẫn tuyệt đối đều bị từ chối; không tự cắt đường dẫn nguy hiểm rồi nhận file.

| Phản hồi | Ý nghĩa |
| --- | --- |
| `OK` | Lưu file và xác minh hash thành công |
| `ERR HASH_MISMATCH` | SHA-256 thực tế không khớp metadata |
| `ERR INVALID_FILENAME` | Tên không hợp lệ hoặc chứa đường dẫn |
| `ERR INVALID_SIZE` | Size âm hoặc vượt 1 GiB |
| `ERR INVALID_HASH` | Hash không phải 64 ký tự hex |
| `ERR INVALID_METADATA` | Metadata bị thiếu, sai định dạng hoặc UTF-8 không hợp lệ |
| `ERR FILE_EXISTS` | File đích đã tồn tại, không ghi đè |
| `ERR INCOMPLETE_FILE` | Client đóng chiều gửi trước khi đủ số byte |
| `ERR TIMEOUT` | Hết thời gian chờ đọc |
| `ERR IO_ERROR` | Lỗi đọc/ghi khác |

File chưa hoàn tất/hash sai được xóa khi phiên kết thúc bình thường hoặc lỗi được xử lý. Nếu tiến trình bị tắt cưỡng bức có thể còn `.receiving-*.part`; file này không được công bố là upload thành công. Thư mục upload do server quản lý, không cho người dùng khác sửa trong khi chạy. SHA-256 kiểm tra tính toàn vẹn, không mã hóa đường truyền hoặc xác thực người gửi.

## Minh chứng kiểm thử

Chạy sau khi biên dịch:

```powershell
python Bai_5/Bai_8/verify.py
```

Kiểm tra file rỗng, văn bản UTF-8 tiếng Việt, ảnh PNG thực, tên chứa `../`, đường dẫn Windows, hash sai, size kiểu long, thiếu byte và file trùng tên. Các ca thành công không gửi EOF trước phản hồi để chứng minh server dùng size làm ranh giới. Bài kiểm tra còn chạy FileClient thật và đối chiếu byte/hash file đã lưu.

Kiểm thử dùng thư mục tạm riêng, không sửa file upload của người dùng. Kết quả chạy thực tế lưu trong `minh_chung.txt`.
