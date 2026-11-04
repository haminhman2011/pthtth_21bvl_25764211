package Bai5;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * BÀI 5 - Cài đặt callback phía client
 * Khi server gọi receiveMessage() thì client in tin nhắn ra màn hình.
 */
public class ChatClientImpl extends UnicastRemoteObject implements ChatClientCallback {

    public ChatClientImpl() throws RemoteException {
        super();
    }

    @Override
    public void receiveMessage(String message) throws RemoteException {
        System.out.println(message);
    }
}
