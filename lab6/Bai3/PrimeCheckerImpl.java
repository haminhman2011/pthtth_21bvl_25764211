package Bai3;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * BÀI 3 - Server cài đặt thuật toán kiểm tra số nguyên tố
 *
 * Số nguyên tố: số tự nhiên lớn hơn 1, chỉ chia hết cho 1 và chính nó.
 * Thuật toán:
 *  - n < 2           -> không phải số nguyên tố
 *  - n = 2           -> là số nguyên tố
 *  - n chẵn (> 2)    -> không phải
 *  - Thử chia n cho các số lẻ i từ 3 đến căn(n). Chia hết -> không phải.
 */
public class PrimeCheckerImpl extends UnicastRemoteObject implements PrimeChecker {

    public PrimeCheckerImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean isPrime(int n) throws RemoteException {
        boolean ketQua = kiemTra(n);
        System.out.println("[Server] Kiem tra " + n + " -> " + (ketQua ? "la so nguyen to" : "khong phai so nguyen to"));
        return ketQua;
    }

    private boolean kiemTra(int n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        // dùng long để i * i không bị tràn số khi n lớn
        for (long i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }
}
