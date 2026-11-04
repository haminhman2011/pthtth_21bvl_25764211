package Bai7;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * BÀI 7 - Server thực hiện các phép tính diện tích
 */
public class GeometryImpl extends UnicastRemoteObject implements Geometry {

    public GeometryImpl() throws RemoteException {
        super();
    }

    @Override
    public double rectangleArea(double width, double height) throws RemoteException {
        double s = width * height;
        System.out.println("[Server] Hinh chu nhat " + width + " x " + height + " -> S = " + s);
        return s;
    }

    @Override
    public double circleArea(double radius) throws RemoteException {
        double s = Math.PI * radius * radius;
        System.out.println("[Server] Hinh tron r = " + radius + " -> S = " + s);
        return s;
    }

    @Override
    public double triangleArea(double base, double height) throws RemoteException {
        double s = 0.5 * base * height;
        System.out.println("[Server] Tam giac day = " + base + ", cao = " + height + " -> S = " + s);
        return s;
    }
}
