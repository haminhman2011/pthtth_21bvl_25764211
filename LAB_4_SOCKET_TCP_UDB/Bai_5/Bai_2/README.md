# Bài 2 — TCP đổi chữ số thành chữ

Biên dịch tại thư mục gốc dự án:

```powershell
javac -encoding UTF-8 -d . Bai_5/Bai_2/DigitServer.java Bai_5/Bai_2/DigitClient.java
```

Mở hai terminal tại thư mục gốc. Chạy server trước:

```powershell
java Bai_5.Bai_2.DigitServer
```

Chạy client ở terminal còn lại:

```powershell
java Bai_5.Bai_2.DigitClient
```

Mặc định kết nối `localhost:5002`. Có thể đổi cổng bằng `java Bai_5.Bai_2.DigitServer 6002` và kết nối bằng `java Bai_5.Bai_2.DigitClient localhost 6002`.

## Giao thức

- Mã hóa UTF-8; mỗi yêu cầu và mỗi phản hồi là một dòng, kết thúc bằng LF hoặc CRLF.
- Chỉ chấp nhận đúng một ký tự ASCII từ `0` đến `9`. Server trả về lần lượt: `không`, `một`, `hai`, `ba`, `bốn`, `năm`, `sáu`, `bảy`, `tám`, `chín`.
- Dữ liệu khác trả đúng `ERR INVALID_DIGIT`. Không loại bỏ khoảng trắng, nên ` 1`, `1 ` và dòng chỉ chứa khoảng trắng đều sai.
- `QUIT` (đúng chữ hoa, không có khoảng trắng) trả `BYE` rồi đóng phiên. Client có thể gửi nhiều yêu cầu trên cùng kết nối trước khi thoát.
- Server phục vụ từng client tuần tự; sau khi client ngắt kết nối, server chờ client tiếp theo. Nhấn Ctrl+C ở terminal server để dừng.

## Minh chứng

Nhập lần lượt `0`, `9`, dòng rỗng (nhấn Enter), `10`, `a`, ` 1`, `1 `, một dấu cách, rồi `QUIT`. Kết quả chạy thực tế và kiểm tra toàn bộ 10 chữ số được lưu trong `minh_chung.txt`.
