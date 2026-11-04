package Bai1;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * BÀI 1 - Interface từ xa (Remote Interface)
 *
 * Quy tắc khi viết interface RMI:
 *  - Interface phải kế thừa (extends) java.rmi.Remote
 *  - Mọi phương thức đều phải khai báo "throws RemoteException"
 *    (vì lời gọi đi qua mạng nên có thể bị lỗi kết nối)
 *
 * Client và Server đều dùng chung interface này.
 */
public interface Hello extends Remote {

    // Phương thức trả về chuỗi "Hello, World!"
    String sayHello() throws RemoteException;
}
