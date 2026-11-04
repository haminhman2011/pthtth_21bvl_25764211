package Bai1;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * BÀI 1 - Lớp cài đặt (triển khai) interface Hello ở phía SERVER
 *
 *  - Kế thừa UnicastRemoteObject để đối tượng được "export" ra mạng,
 *    tức là client ở máy khác có thể gọi được.
 *  - implements Hello để cài đặt phương thức sayHello().
 */
public class HelloImpl extends UnicastRemoteObject implements Hello {

    // Constructor bắt buộc phải throws RemoteException
    // vì constructor của UnicastRemoteObject có thể ném lỗi khi export
    public HelloImpl() throws RemoteException {
        super();
    }

    @Override
    public String sayHello() throws RemoteException {
        // In ra màn hình server để biết là có client vừa gọi
        System.out.println("[Server] Co client vua goi sayHello()");
        return "Hello, World!";
    }
}
