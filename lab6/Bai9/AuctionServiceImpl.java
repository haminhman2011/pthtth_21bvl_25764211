package Bai9;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * BÀI 9 - Server đấu giá
 *
 *  - items     : danh sách sản phẩm (key = mã sản phẩm)
 *  - listeners : danh sách client đã đăng ký nhận thông báo
 *
 * Khi có giá thầu mới cao hơn -> cập nhật giá cao nhất -> thông báo cho tất cả client.
 */
public class AuctionServiceImpl extends UnicastRemoteObject implements AuctionService {

    private final Map<Integer, AuctionItem> items = new LinkedHashMap<>();
    // CopyOnWriteArrayList an toàn khi vừa duyệt vừa thêm/xóa ở nhiều luồng
    private final List<AuctionListener> listeners = new CopyOnWriteArrayList<>();
    private int nextId = 1; // mã sản phẩm tự tăng

    public AuctionServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public int addItem(String seller, String itemName, double startPrice) throws RemoteException {
        AuctionItem item;
        synchronized (this) {
            item = new AuctionItem(nextId++, itemName, seller, startPrice);
            items.put(item.getId(), item);
        }
        String msg = String.format("[THONG BAO] San pham moi #%d '%s' cua %s, gia khoi diem %,.0f",
                item.getId(), itemName, seller, startPrice);
        System.out.println(msg);
        thongBaoTatCa(msg);
        return item.getId();
    }

    @Override
    public synchronized List<AuctionItem> getItems() throws RemoteException {
        // Trả về bản sao danh sách (RMI sẽ tự serialize gửi về client)
        return new ArrayList<>(items.values());
    }

    @Override
    public boolean placeBid(int itemId, String bidder, double amount) throws RemoteException {
        String msg;
        synchronized (this) {
            AuctionItem item = items.get(itemId);
            if (item == null) {
                System.out.println("[Server] " + bidder + " dat gia cho san pham #" + itemId + " -> khong ton tai");
                return false;
            }
            // Giá thầu phải cao hơn giá hiện tại
            if (amount <= item.getCurrentPrice()) {
                System.out.printf("[Server] %s dat %,.0f cho #%d -> THAP HON/BANG gia hien tai %,.0f%n",
                        bidder, amount, itemId, item.getCurrentPrice());
                return false;
            }
            String nguoiCu = item.getHighestBidder();
            item.setHighestBid(amount, bidder);

            msg = String.format("[THONG BAO] %s vua dat gia %,.0f cho san pham #%d '%s'",
                    bidder, amount, itemId, item.getName());
            if (nguoiCu != null && !nguoiCu.equals(bidder)) {
                msg += " (vuot qua " + nguoiCu + ")";
            }
        }
        // Thông báo nằm ngoài synchronized để không giữ khóa lâu khi gọi qua mạng
        System.out.println(msg);
        thongBaoTatCa(msg);
        return true;
    }

    @Override
    public void registerListener(AuctionListener listener) throws RemoteException {
        listeners.add(listener);
        System.out.println("[Server] Co 1 client dang ky nhan thong bao. Tong: " + listeners.size());
    }

    @Override
    public void unregisterListener(AuctionListener listener) throws RemoteException {
        listeners.remove(listener);
        System.out.println("[Server] Co 1 client huy dang ky. Tong: " + listeners.size());
    }

    // Gửi thông báo cho tất cả client, client nào lỗi (đã tắt) thì xóa khỏi danh sách
    private void thongBaoTatCa(String msg) {
        for (AuctionListener l : listeners) {
            try {
                l.notifyMessage(msg);
            } catch (RemoteException e) {
                listeners.remove(l);
                System.out.println("[Server] Xoa 1 client da mat ket noi. Tong: " + listeners.size());
            }
        }
    }
}
