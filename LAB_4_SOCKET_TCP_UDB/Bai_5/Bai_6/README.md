# Bài 6 — Thực nghiệm so sánh TCP và UDP

Chương trình tự mở hai echo server TCP/UDP trên localhost và chạy đo; không cần mở server ở terminal riêng.

Tại thư mục gốc dự án:

```powershell
javac -encoding UTF-8 -d . Bai_5/Bai_6/NetworkComparison.java
java "-Duser.timezone=Asia/Bangkok" Bai_5.Bai_6.NetworkComparison
```

Mặc định chạy **5 lượt cho mỗi giao thức**, mỗi lượt gửi **1.000 thông điệp**, mỗi thông điệp **256 byte**. Có thể tăng số lượt và chọn thư mục lưu kết quả:

```powershell
java "-Duser.timezone=Asia/Bangkok" Bai_5.Bai_6.NetworkComparison 10 Bai_5/Bai_6/lan_chay_10
```

Số lượt phải ít nhất 5. Chạy lại cùng thư mục sẽ ghi đè kết quả trước.

## Phương pháp và thông số

- Địa chỉ `127.0.0.1`, cổng tự cấp; server và client ở cùng một JVM, server chạy trên thread riêng.
- Payload cố định 256 byte: 4 byte số thứ tự và 252 byte `x`. Phản hồi echo cũng 256 byte; kích thước này không bao gồm header mạng.
- TCP dùng frame cố định, đọc đủ 256 byte vì TCP là byte stream. Bật `TCP_NODELAY` ở cả hai đầu. UDP dùng một datagram cho mỗi thông điệp.
- Gửi tuần tự theo **stop-and-wait**: gửi một thông điệp rồi chờ phản hồi trước khi gửi thông điệp kế tiếp. Hai giao thức dùng cùng payload và cách gửi.
- Timeout 1.000 ms cho mỗi phản hồi, tính trên toàn bộ frame TCP hoặc quá trình tìm phản hồi UDP đúng sequence; TCP connect timeout cũng 1.000 ms.
- UDP không gửi lại khi timeout. Chỉ đếm phản hồi khớp số thứ tự và toàn bộ payload; bỏ qua phản hồi trễ, không đếm trùng. TCP lỗi/timeout thì báo thực nghiệm không hoàn tất, tránh dùng kết quả dở dang để so sánh.
- Dùng `System.nanoTime()` đo tổng thời gian từ trước khi gửi thông điệp đầu đến khi nhận phản hồi hoặc timeout thông điệp cuối. Bao gồm tạo payload, gửi, chờ, nhận, xác thực; loại trừ tạo/kết nối/đóng socket và ghi kết quả.
- Warm-up riêng 1.000 thông điệp cho mỗi giao thức trước khi đo, không tính vào bảng kết quả. Lượt lẻ đo TCP trước, lượt chẵn đo UDP trước.

## Kết quả và nhận xét

- `ket_qua.csv`: số lượt, giao thức, số thông điệp, kích thước, timeout, số phản hồi và tổng thời gian từng lượt.
- `ket_qua.txt`: môi trường chạy thực tế (OS, JVM, CPU, múi giờ), phương pháp, bảng kết quả, mean/median/min/max và nhận xét theo dữ liệu đo.

Không kết luận UDP luôn nhanh hơn từ một lần chạy. So sánh phân bố thời gian qua nhiều lượt và số phản hồi nhận được. Localhost thường ít mất gói; kết quả không đại diện cho mạng thực hoặc phép đo thông lượng khi gửi đồng thời. Chi phí JVM, lập lịch thread, tải nền và cấu hình hệ điều hành có thể làm thay đổi kết quả.
