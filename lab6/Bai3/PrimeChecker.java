package Bai3;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * BÀI 3 - Interface từ xa: kiểm tra số nguyên tố
 */
public interface PrimeChecker extends Remote {

    // Trả về true nếu n là số nguyên tố, ngược lại false
    boolean isPrime(int n) throws RemoteException;
}
