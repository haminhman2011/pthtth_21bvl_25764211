package Bai9;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * BÀI 9 - Interface callback phía CLIENT
 *
 * Client (người đấu giá) đăng ký 1 listener với server.
 * Khi có giá thầu mới / sản phẩm mới, server gọi notifyMessage() để
 * thông báo cho TẤT CẢ client theo thời gian thực.
 */
public interface AuctionListener extends Remote {

    void notifyMessage(String message) throws RemoteException;
}
