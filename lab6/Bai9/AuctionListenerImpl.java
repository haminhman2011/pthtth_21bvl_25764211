package Bai9;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * BÀI 9 - Cài đặt callback ở client: in thông báo server gửi tới
 */
public class AuctionListenerImpl extends UnicastRemoteObject implements AuctionListener {

    public AuctionListenerImpl() throws RemoteException {
        super();
    }

    @Override
    public void notifyMessage(String message) throws RemoteException {
        System.out.println();
        System.out.println(">>> " + message);
    }
}
