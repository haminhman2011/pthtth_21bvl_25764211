package Bai6;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * BÀI 6 - Server quản lý 1 tài khoản ngân hàng
 *
 * Các hàm đều "synchronized": nếu 2 client cùng rút tiền 1 lúc
 * thì phải xử lý lần lượt, tránh trường hợp rút quá số dư.
 */
public class BankAccountImpl extends UnicastRemoteObject implements BankAccount {

    private double balance; // số dư

    public BankAccountImpl(double soDuBanDau) throws RemoteException {
        super();
        this.balance = soDuBanDau;
    }

    @Override
    public synchronized double getBalance() throws RemoteException {
        System.out.println("[Server] Xem so du: " + dinhDang(balance));
        return balance;
    }

    @Override
    public synchronized void deposit(double amount) throws RemoteException {
        if (amount <= 0) {
            System.out.println("[Server] Gui tien khong hop le: " + amount);
            return;
        }
        balance += amount;
        System.out.println("[Server] Gui " + dinhDang(amount) + " -> so du: " + dinhDang(balance));
    }

    @Override
    public synchronized boolean withdraw(double amount) throws RemoteException {
        if (amount <= 0 || amount > balance) {
            System.out.println("[Server] Rut " + dinhDang(amount) + " THAT BAI (so du: " + dinhDang(balance) + ")");
            return false;
        }
        balance -= amount;
        System.out.println("[Server] Rut " + dinhDang(amount) + " -> so du: " + dinhDang(balance));
        return true;
    }

    // Định dạng tiền cho dễ đọc, ví dụ 1000000 -> 1,000,000 VND
    public static String dinhDang(double tien) {
        return String.format("%,.0f VND", tien);
    }
}
