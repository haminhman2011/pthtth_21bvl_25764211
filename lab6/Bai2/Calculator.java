package Bai2;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * BÀI 2 - Interface từ xa: tính tổng hai số
 */
public interface Calculator extends Remote {

    // Trả về tổng a + b
    int add(int a, int b) throws RemoteException;
}
