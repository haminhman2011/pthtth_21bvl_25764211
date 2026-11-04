# Bài 5 – Chat Đơn Giản với RMI

## Đề bài
Tạo ứng dụng **chat 1-1 giữa client và server** thông qua RMI.
- Interface có phương thức `void sendMessage(String message)`.
- Server nhận tin nhắn từ client và phản hồi.
- Client nhập tin nhắn và hiển thị phản hồi.

## Các file trong bài
| File | Vai trò |
|------|---------|
| `ChatServer.java` | Interface phía server: `sendMessage()`, `registerClient()`, `unregisterClient()` |
| `ChatClientCallback.java` | Interface phía client: `receiveMessage()` (server gọi ngược lại) |
| `ChatServerImpl.java` | Cài đặt server, giữ 1 client đang chat, có hàm `replyToClient()` để trả lời |
| `ChatClientImpl.java` | Cài đặt callback ở client, in tin nhắn server gửi về |
| `ChatServerMain.java` | Chạy server, người ở server gõ tin nhắn để trả lời |
| `ChatClientMain.java` | Chạy client, gõ tin nhắn để gửi |

## Giải thích (quan trọng)
Hàm `sendMessage()` trả về `void`, nghĩa là server **không trả lời trực tiếp** qua giá trị trả về được.
Để server "phản hồi" được, em dùng kỹ thuật **callback**:

1. Client tạo 1 đối tượng từ xa của riêng mình (`ChatClientImpl`) và gửi cho server qua `registerClient()`.
2. Khi client gõ tin nhắn → client gọi `server.sendMessage(msg)` → server in ra.
3. Khi người ở server gõ trả lời → server gọi `client.receiveMessage(msg)` → client in ra.

```
 CLIENT                                     SERVER
 registerClient(ten, callback) --------->   lưu lại callback
 sendMessage("Xin chao")       --------->   in "Man: Xin chao"
 in "Server: Chao ban"         <---------   callback.receiveMessage("Server: Chao ban")
```
Như vậy 2 bên có thể chat qua lại bất cứ lúc nào, không cần chờ nhau.

## Cách chạy
> Mở terminal ở **thư mục `lab6`**.

**Bước 1 – Biên dịch:**
```bash
javac Bai5/*.java
```

**Bước 2 – Terminal 1, chạy server:**
```bash
java Bai5.ChatServerMain
```

**Bước 3 – Terminal 2, chạy client:**
```bash
java Bai5.ChatClientMain
```

**Bước 4 – Chat:**
- Ở terminal client: nhập tên, rồi gõ tin nhắn + Enter.
- Ở terminal server: gõ tin nhắn + Enter để trả lời.
- Client gõ `bye` để thoát, server gõ `exit` để tắt.

## Kết quả mẫu
Terminal client:
```
===== CHAT CLIENT =====
Nhap ten cua ban: Man
Da vao phong chat. Go tin nhan roi Enter de gui, go 'bye' de thoat.
Xin chao server
Server: Chao Man, ban can gi?
Cho minh hoi bai RMI
Server: OK ban hoi di
bye
Da thoat chat. Tam biet!
```
Terminal server:
```
===== CHAT SERVER =====
Server dang chay tai cong 1099, ten dich vu: ChatService
Dang cho client vao chat... (go 'exit' de tat server)
>>> Man da vao phong chat. Ban co the go tin nhan de tra loi.
Man: Xin chao server
Chao Man, ban can gi?
Man: Cho minh hoi bai RMI
OK ban hoi di
>>> Man da roi phong chat.
```

## Lỗi thường gặp
- `Connection refused` → chưa chạy server.
- `Port already in use: 1099` → tắt server của bài khác đang chạy.
- Server gõ tin nhắn khi chưa có client → báo "Chua co client nao ket noi".
- Chạy 2 máy khác nhau: vì server phải gọi ngược về client, nên nếu client có nhiều card mạng có thể phải chạy client với
  `java -Djava.rmi.server.hostname=<IP_client> Bai5.ChatClientMain <IP_server>`.
