package Bai5;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * BÀI 5 - Interface từ xa phía SERVER (client gọi các hàm này)
 *
 * sendMessage(String message) là hàm đề bài yêu cầu.
 * Vì sendMessage trả về void nên server không trả lời trực tiếp được,
 * => client phải "đăng ký" 1 đối tượng callback (ChatClientCallback) với server,
 *    khi server muốn phản hồi thì server gọi ngược lại callback.receiveMessage().
 */
public interface ChatServer extends Remote {

    // Client gửi tin nhắn lên server
    void sendMessage(String message) throws RemoteException;

    // Client đăng ký với server: gửi tên + đối tượng callback của mình
    void registerClient(String name, ChatClientCallback client) throws RemoteException;

    // Client báo thoát
    void unregisterClient() throws RemoteException;
}
