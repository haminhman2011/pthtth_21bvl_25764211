# Bài 9 – Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

## Đề bài
Xây dựng hệ thống đấu giá trực tuyến bằng Java RMI.
1. **Chủ sở hữu (Seller)**: thêm sản phẩm vào hệ thống đấu giá (tên sản phẩm, giá khởi điểm).
2. **Người đấu giá (Bidder)**: xem danh sách sản phẩm, đưa ra giá thầu cao hơn giá hiện tại.
3. **Server**: quản lý sản phẩm & giá thầu, cập nhật giá cao nhất, **thông báo khi có người đặt giá cao hơn**.

## Các file trong bài
| File | Vai trò |
|------|---------|
| `AuctionItem.java` | Sản phẩm đấu giá (`implements Serializable` để gửi qua mạng) |
| `AuctionService.java` | Interface từ xa phía server: `addItem`, `getItems`, `placeBid`, `registerListener`, `unregisterListener` |
| `AuctionListener.java` | Interface callback phía client: `notifyMessage()` |
| `AuctionServiceImpl.java` | Server: lưu sản phẩm, kiểm tra giá thầu, gửi thông báo cho mọi client |
| `AuctionListenerImpl.java` | Client in thông báo khi server gọi tới |
| `AuctionServer.java` | Chạy server, đăng ký tên `AuctionService` |
| `SellerClient.java` | Chương trình cho người bán |
| `BidderClient.java` | Chương trình cho người đấu giá (nhận thông báo realtime) |

## Giải thích
- **Gửi đối tượng qua mạng**: `getItems()` trả về `List<AuctionItem>`, nên `AuctionItem` phải `implements Serializable`.
- **Kiểm tra giá thầu**: giá mới phải **lớn hơn** giá hiện tại (`amount > currentPrice`), nếu không thì trả về `false`.
- **Thông báo realtime (callback)**:
  1. Mỗi Bidder khi mở lên sẽ tạo `AuctionListenerImpl` và gọi `registerListener()` để gửi cho server.
  2. Khi có giá thầu mới hoặc sản phẩm mới, server duyệt danh sách listener và gọi `notifyMessage()` cho từng client.
  3. Client nào đã tắt (gọi lỗi `RemoteException`) thì server tự xóa khỏi danh sách.
- **Đồng bộ**: phần kiểm tra + cập nhật giá nằm trong `synchronized` để 2 người đặt cùng lúc không bị sai giá cao nhất.

```
 BidderClient A ---- placeBid(#1, A, 600) ----> AuctionServer
                                                  | cập nhật giá cao nhất = 600
 BidderClient A <--- notifyMessage(...) ----------|
 BidderClient B <--- notifyMessage(...) ----------|   "A vua dat gia 600 cho #1"
```

## Cách chạy
> Mở terminal ở **thư mục `lab6`**. Bài này nên mở **4 terminal** để thấy rõ thông báo realtime.

**Bước 1 – Biên dịch:**
```bash
javac Bai9/*.java
```

**Bước 2 – Terminal 1, chạy server:**
```bash
java Bai9.AuctionServer
```

**Bước 3 – Terminal 2 và 3, chạy 2 người đấu giá:**
```bash
java Bai9.BidderClient
```
(nhập tên khác nhau, ví dụ `Man` và `Lan`)

**Bước 4 – Terminal 4, chạy người bán:**
```bash
java Bai9.SellerClient
```

**Bước 5 – Thử:**
1. Seller chọn `1` thêm sản phẩm, ví dụ `Dong ho` giá `500000` → 2 Bidder đều nhận được thông báo sản phẩm mới.
2. Bidder `Man` chọn `2`, đặt giá `#1` = `600000` → thành công, `Lan` nhận được thông báo.
3. Bidder `Lan` đặt `#1` = `550000` → **thất bại** vì thấp hơn giá hiện tại.
4. Bidder `Lan` đặt `#1` = `700000` → thành công, `Man` nhận thông báo "Lan ... (vuot qua Man)".
5. Chọn `1` để xem danh sách và giá cao nhất hiện tại.

## Kết quả mẫu
Terminal Bidder `Man`:
```
>>> [THONG BAO] San pham moi #1 'Dong ho' cua Tuan, gia khoi diem 500,000

Chon: 2
Nhap ma san pham (so sau dau #): 1
Nhap gia thau: 600000
>>> [THONG BAO] Man vua dat gia 600,000 cho san pham #1 'Dong ho'
=> Dat gia thanh cong! Ban dang la nguoi tra gia cao nhat.

>>> [THONG BAO] Lan vua dat gia 700,000 cho san pham #1 'Dong ho' (vuot qua Man)
```
Xem danh sách:
```
=== DANH SACH SAN PHAM DANG DAU GIA ===
#1   Dong ho         | Nguoi ban: Tuan     | Khoi diem:      500,000 | Hien tai:      700,000 | Cao nhat: Lan
```

## Lỗi thường gặp
- `Connection refused` → chưa chạy server.
- `Port already in use: 1099` → tắt server của bài khác đang chạy (`Ctrl + C`).
- Chạy trên nhiều máy khác nhau mà Bidder không nhận được thông báo → chạy Bidder kèm IP của chính máy đó:
  `java -Djava.rmi.server.hostname=<IP_may_bidder> Bai9.BidderClient <IP_server>`, và tắt tường lửa (firewall) nếu cần.
