package Bai9;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * BÀI 9 - Interface từ xa phía SERVER đấu giá
 */
public interface AuctionService extends Remote {

    // (Seller) Thêm sản phẩm, trả về mã sản phẩm vừa thêm
    int addItem(String seller, String itemName, double startPrice) throws RemoteException;

    // (Bidder) Xem danh sách sản phẩm đang đấu giá
    List<AuctionItem> getItems() throws RemoteException;

    // (Bidder) Đặt giá thầu, phải CAO HƠN giá hiện tại thì mới thành công
    boolean placeBid(int itemId, String bidder, double amount) throws RemoteException;

    // Đăng ký / hủy đăng ký nhận thông báo
    void registerListener(AuctionListener listener) throws RemoteException;

    void unregisterListener(AuctionListener listener) throws RemoteException;
}
