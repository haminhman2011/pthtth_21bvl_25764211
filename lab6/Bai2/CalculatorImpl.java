package Bai2;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * BÀI 2 - Cài đặt phương thức add() ở phía server
 */
public class CalculatorImpl extends UnicastRemoteObject implements Calculator {

    public CalculatorImpl() throws RemoteException {
        super();
    }

    @Override
    public int add(int a, int b) throws RemoteException {
        int tong = a + b;
        System.out.println("[Server] Client yeu cau: " + a + " + " + b + " = " + tong);
        return tong;
    }
}
