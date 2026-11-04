# Bài 1 – Máy chủ RMI trả về chuỗi "Hello, World!"

## Đề bài
Viết ứng dụng RMI trong đó **client** gọi phương thức từ **server** để nhận về chuỗi `"Hello, World!"`.
- Tạo interface chứa phương thức `sayHello()`.
- Triển khai phương thức này ở server.
- Client gọi phương thức từ xa.

## Các file trong bài
| File | Vai trò |
|------|---------|
| `Hello.java` | Interface từ xa (extends `Remote`), có phương thức `String sayHello()` |
| `HelloImpl.java` | Lớp cài đặt interface ở server (extends `UnicastRemoteObject`) |
| `HelloServer.java` | Tạo Registry cổng `1099`, đăng ký đối tượng với tên `HelloService` |
| `HelloClient.java` | Tìm `HelloService` trong Registry rồi gọi `sayHello()` |

## Kiến thức RMI dùng trong bài
1. **Remote Interface**: phải `extends Remote`, mọi phương thức phải `throws RemoteException`.
2. **Remote Object**: lớp cài đặt `extends UnicastRemoteObject` để client gọi được qua mạng.
3. **Registry**: giống "danh bạ", server đăng ký (`rebind`) đối tượng với 1 cái tên, client tìm (`lookup`) theo tên đó.
4. **Stub**: đối tượng client nhận về sau khi `lookup`. Gọi hàm trên stub thì RMI tự gửi lời gọi qua mạng tới server.

```
 CLIENT                                 SERVER
 ------                                 ------
 lookup("HelloService")  ---------->    Registry (cổng 1099)
 stub.sayHello()         ---------->    HelloImpl.sayHello()
 "Hello, World!"         <----------    return "Hello, World!"
```

## Cách chạy

> **Lưu ý:** Các file có dòng `package Bai1;` nên phải mở terminal ở **thư mục `lab6`** (thư mục cha của `Bai1`), KHÔNG `cd` vào trong `Bai1`.

**Bước 1 – Mở terminal tại thư mục `lab6` và biên dịch:**
```bash
javac Bai1/*.java
```

**Bước 2 – Chạy server (terminal thứ 1):**
```bash
java Bai1.HelloServer
```

**Bước 3 – Mở terminal thứ 2 (cũng ở thư mục `lab6`), chạy client:**
```bash
java Bai1.HelloClient
```

Nếu server ở máy khác thì truyền IP của server vào:
```bash
java Bai1.HelloClient 192.168.1.10
```

## Kết quả mẫu
Terminal server:
```
===== HELLO SERVER =====
Server da san sang tai cong 1099
Ten dich vu: HelloService
Dang cho client ket noi... (Ctrl + C de tat server)
[Server] Co client vua goi sayHello()
```
Terminal client:
```
===== HELLO CLIENT =====
Da ket noi toi server: localhost:1099
Ket qua nhan tu server: Hello, World!
```

## Lỗi thường gặp
- `Connection refused` → chưa chạy server, hoặc server đã bị tắt.
- `Port already in use: 1099` → đang có 1 server khác (của bài khác) còn chạy, bấm `Ctrl + C` để tắt nó trước.
- `Could not find or load main class` → đang đứng sai thư mục, phải đứng ở `lab6` và gõ có tiền tố `Bai1.`.
- Tắt server: bấm `Ctrl + C` trong terminal server.
