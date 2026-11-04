# LAB 06 – RMI (Remote Method Invocation)

| Bài | Nội dung | Server | Client |
|-----|----------|--------|--------|
| [Bai1](Bai1/README.md) | Hello, World! | `Bai1.HelloServer` | `Bai1.HelloClient` |
| [Bai2](Bai2/README.md) | Tính tổng hai số | `Bai2.CalculatorServer` | `Bai2.CalculatorClient` |
| [Bai3](Bai3/README.md) | Kiểm tra số nguyên tố | `Bai3.PrimeServer` | `Bai3.PrimeClient` |
| [Bai4](Bai4/README.md) | Quản lý danh bạ | `Bai4.PhoneBookServer` | `Bai4.PhoneBookClient` |
| [Bai5](Bai5/README.md) | Chat 1-1 (callback) | `Bai5.ChatServerMain` | `Bai5.ChatClientMain` |
| [Bai6](Bai6/README.md) | Tài khoản ngân hàng | `Bai6.BankServer` | `Bai6.BankClient` |
| [Bai7](Bai7/README.md) | Diện tích hình học | `Bai7.GeometryServer` | `Bai7.GeometryClient` |
| [Bai8](Bai8/README.md) | Đặt vé máy bay | `Bai8.FlightServer` | `Bai8.FlightClient` |
| [Bai9](Bai9/README.md) | Đấu giá trực tuyến (callback) | `Bai9.AuctionServer` | `Bai9.SellerClient`, `Bai9.BidderClient` |

## Cách chạy chung (bài nào cũng giống nhau)
Luôn đứng ở **thư mục `lab6`** (vì mỗi file có `package BaiX;`):
```bash
javac BaiX/*.java          # 1. biên dịch
java BaiX.<TenServer>      # 2. terminal 1: chạy server
java BaiX.<TenClient>      # 3. terminal 2: chạy client
```
- Tất cả các bài dùng chung cổng **1099** → chạy bài mới thì phải tắt server bài cũ (`Ctrl + C`).
- Server tự tạo Registry bằng `LocateRegistry.createRegistry(1099)` nên **không cần** chạy lệnh `rmiregistry` riêng.
- Client mặc định kết nối `localhost`; chạy khác máy thì thêm IP server: `java BaiX.<TenClient> 192.168.1.10`.

## Các bước làm 1 bài RMI
1. Viết **interface** `extends Remote`, mọi phương thức `throws RemoteException`.
2. Viết lớp **cài đặt** `extends UnicastRemoteObject implements <Interface>`.
3. **Server**: tạo đối tượng → `createRegistry(1099)` → `rebind("TenDichVu", doiTuong)`.
4. **Client**: `getRegistry(host, 1099)` → `lookup("TenDichVu")` → ép kiểu về interface → gọi hàm.
5. Đối tượng gửi qua mạng (tham số / kết quả trả về) phải `implements Serializable` (xem Bài 9).
6. Muốn server chủ động gửi về client → dùng **callback**: client cũng tạo 1 remote object rồi gửi cho server (xem Bài 5, Bài 9).
