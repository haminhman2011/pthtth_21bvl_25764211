# Bài 1 — Host và URI Inspector

Chương trình nhận **hai tham số riêng biệt**: hostname cần phân giải và URI cần phân tích. Dùng `InetAddress.getAllByName` để in mọi IP được trả về, phân loại IPv4/IPv6, loopback và site local; dùng `java.net.URI` để in scheme, host, port, path, query, fragment. Không tải nội dung trang web.

## Biên dịch và chạy

Mở terminal tại thư mục gốc `LAB_4_SOCKET_TCP_UDB`:

```powershell
javac -encoding UTF-8 -d . Bai_5/bai_1/HostUriInspector.java
java Bai_5.bai_1.HostUriInspector localhost "https://example.com:8443/docs?q=java#intro"
```

URI phải nằm trong dấu ngoặc kép khi có ký tự đặc biệt. Hostname và host trong URI được kiểm tra độc lập, không bắt buộc trùng nhau. URI tương đối vẫn hợp lệ theo `java.net.URI`; thành phần vắng mặt hiển thị `(không có)`, port không khai báo hiển thị `(không chỉ định)`. Chương trình trả mã thoát `0` khi thành công, `1` khi lỗi.

## Sáu trường hợp minh chứng

Ba ca hợp lệ (dùng IP cục bộ để kết quả không phụ thuộc DNS Internet):

```powershell
java Bai_5.bai_1.HostUriInspector 127.0.0.1 "https://example.com:8443/docs?q=java#intro"
java Bai_5.bai_1.HostUriInspector ::1 "http://[::1]:8080/test#ipv6"
java Bai_5.bai_1.HostUriInspector 192.168.1.10 "https://example.com/"
```

Kết quả mong đợi: ca 1 là IPv4, loopback `true`, site local `false`; ca 2 là IPv6, loopback `true`, site local `false`; ca 3 là IPv4, loopback `false`, site local `true`. Địa chỉ IP chỉ được phân tích, không cần có máy đang hoạt động tại địa chỉ đó.

Ba ca lỗi:

```powershell
java Bai_5.bai_1.HostUriInspector localhost
java Bai_5.bai_1.HostUriInspector localhost "http://example.com/a b"
java Bai_5.bai_1.HostUriInspector no-such-host.invalid "https://example.com/"
```

Lần lượt báo: thiếu tham số; URI sai cú pháp do chứa dấu cách; hostname không phân giải được. Xem kết quả chạy thực tế trong `minh_chung.txt`.
