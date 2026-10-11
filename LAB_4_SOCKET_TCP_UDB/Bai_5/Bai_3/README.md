# Bài 3 — Dịch vụ ngày giờ TCP và UDP

Chạy các lệnh từ thư mục gốc dự án. Biên dịch:

```powershell
javac -encoding UTF-8 -d . Bai_5/Bai_3/*.java
```

## TCP

Mở terminal server:

```powershell
java Bai_5.Bai_3.TcpDateTimeServer
```

Mở terminal client:

```powershell
java Bai_5.Bai_3.TcpDateTimeClient
```

Nhập lần lượt `DATE`, `TIME`, `DATETIME`, `QUIT`. Mỗi thông điệp là một dòng UTF-8. `QUIT` nhận `BYE` rồi đóng kết nối. Có thể gửi nhiều lệnh trong một phiên. Server phục vụ tuần tự các client.

## UDP

Mở terminal server:

```powershell
java Bai_5.Bai_3.UdpDateTimeServer
```

Mở terminal client:

```powershell
java Bai_5.Bai_3.UdpDateTimeClient
```

Mỗi datagram chứa nguyên một lệnh UTF-8, không thêm ký tự xuống dòng. Các lệnh hợp lệ là `DATE`, `TIME`, `DATETIME`. UDP không có phiên kết nối và không có lệnh `QUIT`; gửi `QUIT` nhận `ERR UNKNOWN_COMMAND`. Nhấn Ctrl+C để thoát client hoặc server.

Cả hai giao thức mặc định dùng cổng `5003` (cổng TCP và UDP độc lập). Server nhận tham số `[port]`, client nhận `[host] [port]`, ví dụ `java Bai_5.Bai_3.UdpDateTimeClient localhost 6003`.

## Định dạng phản hồi

| Lệnh | Mẫu DateTimeFormatter | Ví dụ |
| --- | --- | --- |
| DATE | `dd MM yyyy` | `11 10 2026` |
| TIME | `HH mm ss` | `15 40 05` |
| DATETIME | `dd MM yyyy HH mm ss` | `11 10 2026 15 40 05` |

Ngày giờ lấy theo đồng hồ và múi giờ của máy chạy server. Lệnh phân biệt hoa/thường, không tự bỏ khoảng trắng. Lệnh sai hoặc rỗng trả `ERR UNKNOWN_COMMAND`.

## So sánh khi dừng server lúc client hoạt động

Để minh chứng thủ công: giữ client đang chạy, gửi `DATE`, dừng server bằng Ctrl+C ở terminal server, rồi gửi `TIME` từ client.

- **TCP:** Có kết nối. Khi trao đổi tiếp, client có thể nhận EOF (server đóng kết nối), lỗi socket (ví dụ connection reset), hoặc hết thời gian chờ nếu chưa nhận được tín hiệu đóng. Client thông báo rồi kết thúc phiên; muốn trao đổi lại phải mở client mới. Khi client đang chờ nhập bàn phím, chương trình chỉ phát hiện mất kết nối lúc thực hiện trao đổi tiếp.
- **UDP:** Không có kết nối nên gửi datagram thành công không chứng minh server còn hoạt động. Client chờ phản hồi tối đa 3 giây; nếu không nhận được thì báo timeout và tiếp tục cho nhập lệnh. Có thể do server dừng, mất gói hoặc mạng chậm. Khi server chạy lại, client cũ có thể gửi lệnh mới mà không cần tạo phiên mới. UDP không đảm bảo giao hàng, thứ tự hoặc chống phản hồi trễ từ yêu cầu trước.

Kết quả chạy thực tế được lưu ở `minh_chung.txt`. Kiểm tra lại sau khi biên dịch bằng:

```powershell
python Bai_5/Bai_3/verify.py
```
