package Bai6;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * BÀI 6 - Interface từ xa: tài khoản ngân hàng
 */
public interface BankAccount extends Remote {

    // Xem số dư hiện tại
    double getBalance() throws RemoteException;

    // Gửi tiền vào tài khoản
    void deposit(double amount) throws RemoteException;

    // Rút tiền, trả về true nếu rút được, false nếu không đủ tiền / số tiền không hợp lệ
    boolean withdraw(double amount) throws RemoteException;
}
