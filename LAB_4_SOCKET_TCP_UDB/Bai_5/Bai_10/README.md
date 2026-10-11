# Bài 10 — UDP discovery kết hợp TCP service

Biên dịch tại thư mục gốc:

```powershell
javac -encoding UTF-8 -d . Bai_5/Bai_10/*.java
java Bai_5.Bai_10.DiscoveryClient --list
```

Chọn tên hoặc index interface IPv4 đang hoạt động từ danh sách Java. Thử trên cùng máy bằng interface có `loopback=true` (ví dụ `loopback_0` trên Windows, `lo` trên Linux).

## Chạy hai server và một client

Thay `loopback_0` bằng interface thực tế. Mở hai terminal server, dùng cổng TCP khác nhau nhưng **cùng cổng UDP discovery**:

```powershell
java Bai_5.Bai_10.DiscoveryServer loopback_0 6010 LabA 1.0
```

```powershell
java Bai_5.Bai_10.DiscoveryServer loopback_0 6011 LabB 1.0
```

Terminal client:

```powershell
java Bai_5.Bai_10.DiscoveryClient loopback_0
```

Client chờ hết cửa sổ discovery, hiển thị tất cả server tìm được rồi cho chọn số server. Sau khi kết nối TCP, nhập:

```text
ECHO Xin chào Việt Nam
TIME
QUIT
```

Mỗi lần chạy client chọn một server; chạy lại để chọn server khác. Nhấn Ctrl+C ở server để dừng.

## Giao thức

1. Client gửi datagram UTF-8 **`DISCOVER_SERVICE`** đến multicast `239.255.0.1:5010`, TTL=1, chỉ định interface, bật multicast loopback.
2. Mỗi server tham gia group trên cùng cổng UDP, trả unicast đến địa chỉ/cổng nguồn của yêu cầu. Nội dung đúng mẫu **`SERVICE tên_dịch_vụ tcp_port phiên_bản`**, ví dụ `SERVICE LabA 6010 1.0`.
3. Client lấy **`DatagramPacket.getAddress()` của phản hồi** làm địa chỉ TCP, dùng `tcp_port` từ metadata. Không kết nối đến group multicast, không lấy địa chỉ IP từ chuỗi SERVICE và không dùng địa chỉ mặc định thay cho nguồn phản hồi.
4. TCP service gửi `WELCOME tên phiên_bản`. Lệnh `TIME` trả `OK timestamp` ISO-8601; `ECHO nội_dung` trả `OK nội_dung`; `QUIT` trả `BYE` và đóng phiên. Lệnh sai trả `ERR UNKNOWN_COMMAND`. Mỗi thông điệp TCP một dòng UTF-8.

Server có TCP thread pool tối đa 16 phiên; phiên vượt giới hạn nhận `ERR SERVER_BUSY`. TCP server timeout đọc 60 giây. UDP discovery hoạt động độc lập với các phiên TCP. Server đặt reuse-address trước bind và join/leave group với `NetworkInterface` rõ ràng; cleanup rời group trước đóng socket, kể cả khi Ctrl+C có chạy shutdown hook.

## Timeout và phản hồi trùng

- Discovery mặc định **2.000 ms tổng cộng**, đo bằng `System.nanoTime`, không gia hạn khi nhận gói mới.
- Client gửi tối đa 3 truy vấn ở đầu và tại khoảng 1/3, 2/3 cửa sổ để giảm ảnh hưởng mất gói UDP. Không dừng sau phản hồi đầu tiên, nhờ vậy có thể phát hiện nhiều server.
- Khóa loại trùng: **IP nguồn + cổng TCP + tên dịch vụ + phiên bản**. Hai server cùng máy nhưng khác cổng TCP vẫn hiển thị riêng. IP nguồn lấy từ datagram; cổng nguồn UDP không được dùng làm cổng TCP.
- Phản hồi sai cấu trúc, sai UTF-8, port ngoài 1–65535 hoặc datagram vượt 1.400 byte bị bỏ qua. Client báo số phản hồi trùng và sai định dạng.
- Tên và phiên bản gồm 1–64 ký tự ASCII chữ/số/`_`/`-`/`.`. Danh sách được sắp xếp ổn định trước khi chọn.
- Nếu không có phản hồi, client báo 0 dịch vụ rồi thoát. TCP connect timeout 3 giây, read timeout 5 giây. Server có thể ngừng hoạt động sau discovery; client sẽ báo lỗi khi kết nối/trao đổi.

Tham số:

```text
DiscoveryServer <interface> [tcp_port=6010] [name=TimeEcho] [version=1.0] [udp_port=5010]
DiscoveryClient <interface> [timeout_ms=2000] [udp_port=5010]
```

Timeout discovery cho phép 1–60.000 ms. Hai bên phải chọn cùng cổng UDP; từng server cần cổng TCP không bị trùng trên máy chạy.

## Mạng lab và giới hạn

Nếu loopback multicast không được OS hỗ trợ, dùng interface Ethernet/LAN của máy; có thể chạy hai server trên hai máy cùng VLAN. Mỗi máy chọn interface LAN của chính nó. Kiểm tra multicast, quyền Java/firewall UDP 5010 và TCP các cổng dịch vụ; Wi-Fi isolation hoặc VPN có thể ngăn phản hồi. Không nhận được dịch vụ không đồng nghĩa server TCP chắc chắn đã dừng.

Discovery dùng UDP nên không đảm bảo thấy mọi server; cửa sổ timeout chỉ thu các phản hồi đến kịp. Dữ liệu discovery chưa được xác thực: ứng dụng bài tập không nên xem quảng bá là bằng chứng danh tính của server. Chỉ chọn các dịch vụ trong mạng lab tin cậy.

## Minh chứng kiểm thử

```powershell
python Bai_5/Bai_10/verify.py
```

Script mặc định dùng loopback, hoặc nhận interface lab: `python Bai_5/Bai_10/verify.py <interface>`. Kết quả lưu `minh_chung.txt`, gồm hai server được hiển thị, loại trùng qua ba truy vấn, kết nối TCP tới cả hai lựa chọn bằng địa chỉ nguồn, ECHO tiếng Việt, TIME, QUIT, timeout khi không còn server. Trên loopback còn kiểm tra phản hồi sai cấu trúc/port/UTF-8.
