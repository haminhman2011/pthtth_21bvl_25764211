# Bài 9 — Multicast thông báo

Biên dịch tại thư mục gốc dự án:

```powershell
javac -encoding UTF-8 -d . Bai_5/Bai_9/*.java
java Bai_5.Bai_9.MulticastReceiver --list
```

Chọn **name hoặc index do Java liệt kê**, có `up=true`, có địa chỉ IPv4. Không nhầm tên hiển thị của Windows với `NetworkInterface.getName()`. Sender và receiver trên cùng máy chọn cùng interface; trên các máy khác nhau chọn interface LAN tương ứng của từng máy.

## Phương án thử trên cùng máy (localhost)

Chọn interface có `loopback=true`, ví dụ `loopback_0` trên Windows hoặc `lo` trên Linux. Mở hai terminal receiver trước, thay `loopback_0` bằng tên thực tế trên máy:

```powershell
java Bai_5.Bai_9.MulticastReceiver loopback_0
```

Terminal thứ ba:

```powershell
java Bai_5.Bai_9.MulticastSender loopback_0
```

Nhập thông báo tiếng Việt. Cả hai receiver phải in `RECEIVED ...` với cùng nội dung. Sender vẫn gửi đến **239.255.0.1**, không thay bằng unicast 127.0.0.1; dùng interface loopback và bật multicast loopback để thử cục bộ. Khả năng multicast qua interface loopback phụ thuộc hệ điều hành; nếu bị từ chối hoặc không nhận được, dùng phương án phòng lab bên dưới.

Thử tự động hai receiver, ba thông báo, rồi timeout không có sender:

```powershell
python Bai_5/Bai_9/verify.py
```

Script tự chọn interface loopback, hoặc nhận tên interface do người dùng chỉ định:

```powershell
python Bai_5/Bai_9/verify.py ethernet_32770
```

Tên ở ví dụ chỉ minh họa, phải chọn interface đang hoạt động từ `--list`. Kết quả thực tế lưu trong `minh_chung.txt`.

## Phương án mạng phòng lab

1. Dùng các máy cùng LAN/VLAN, ưu tiên Ethernet có dây; mỗi máy chạy `--list` để chọn interface IPv4 của LAN.
2. Chạy ít nhất hai receiver (hai máy hoặc hai tiến trình), cùng group `239.255.0.1` và cổng `5009`.
3. Chạy sender trên interface LAN của máy gửi, nhập thông báo có mã thử riêng để phân biệt các lần chạy.
4. Đối chiếu nội dung, địa chỉ nguồn và số bản tin ở cả hai receiver. Ghi thời điểm, OS/Java, interface, cổng và kết quả; không suy ra đã nhận chỉ từ dòng `SENT`.

Group `239.255.0.1` thuộc dải multicast phạm vi quản trị cục bộ. Sender đặt TTL=1 để giới hạn phạm vi thử trên liên kết cục bộ. TTL không thay cho chính sách quản trị mạng.

## Giao thức và vòng đời socket

- Một thông báo UTF-8 trên một datagram, sender giới hạn 1.400 byte nội dung để giảm nguy cơ phân mảnh.
- Receiver đặt `setReuseAddress(true)` **trước bind**, cho phép ít nhất hai tiến trình cùng cổng (tùy hỗ trợ OS).
- Receiver gọi `joinGroup(InetSocketAddress, NetworkInterface)` với interface rõ ràng; cleanup gọi `leaveGroup` với cùng group/interface **trước** `close()`.
- Cleanup chạy khi nhận đủ bản tin, lỗi hoặc timeout trong chế độ giới hạn số bản tin. Shutdown hook xử lý Ctrl+C khi JVM còn thực hiện hook; kill cưỡng bức hoặc mất điện không bảo đảm chạy mã cleanup.
- Receiver: `<interface> [port=5009] [count=0] [timeout_ms=10000]`. `count=0` nhận liên tục, báo timeout rồi tiếp tục; `count>0` thoát khi đủ số bản tin hoặc lần chờ timeout đầu tiên.
- Sender: `<interface> [port=5009]`. `QUIT` là lệnh thoát sender cục bộ, không gửi đến group. Nhấn Ctrl+C để dừng receiver.

## Khi multicast bị chặn

| Hiện tượng/môi trường | Kiểm tra và phương án |
| --- | --- |
| Wi-Fi có client isolation hoặc lọc multicast | Cùng SSID chưa chắc cùng miền multicast; thử loopback hoặc Ethernet phòng lab |
| VPN chọn sai tuyến/interface hoặc không chuyển multicast | Chỉ định interface LAN rõ ràng; nếu được phép, ngắt VPN để đối chiếu cùng phép thử |
| Firewall chặn UDP hoặc Java | Kiểm tra quyền Java và rule UDP inbound cho cổng thử trên mạng lab được phép; không cần tắt toàn bộ firewall |
| JOIN thành công, SENT thành công, receiver timeout | Chưa chứng minh đường truyền hoạt động; đối chiếu group/cổng/interface, receiver phải join trước khi gửi |
| Loopback không hỗ trợ multicast trên OS | Chuyển sang card LAN đang hoạt động và hai receiver trên cùng máy hoặc cùng mạng lab |

Ghi nhận **quan sát** riêng với **nguyên nhân đã xác minh**. Ví dụ: “Wi-Fi interface X, hai receiver join thành công nhưng timeout; chưa xác định firewall hay AP lọc multicast; thử loopback nhận được 3/3.” Chỉ kết luận nguyên nhân sau phép đối chiếu có kiểm soát. Bài thử không tự thay đổi firewall/VPN và không giả lập kết quả mạng thật.
