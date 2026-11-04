package Bai5;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * BÀI 5 - Interface từ xa phía CLIENT (server gọi ngược lại hàm này)
 *
 * Đây gọi là kỹ thuật "callback": client cũng trở thành 1 đối tượng từ xa
 * để server có thể chủ động gửi tin nhắn phản hồi về cho client.
 */
public interface ChatClientCallback extends Remote {

    // Server gọi hàm này để gửi tin nhắn phản hồi cho client
    void receiveMessage(String message) throws RemoteException;
}
