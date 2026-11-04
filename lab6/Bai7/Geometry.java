package Bai7;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * BÀI 7 - Interface từ xa: tính diện tích hình học
 */
public interface Geometry extends Remote {

    // Diện tích hình chữ nhật = dài x rộng
    double rectangleArea(double width, double height) throws RemoteException;

    // Diện tích hình tròn = PI x r x r
    double circleArea(double radius) throws RemoteException;

    // Diện tích tam giác = 1/2 x đáy x chiều cao
    double triangleArea(double base, double height) throws RemoteException;
}
