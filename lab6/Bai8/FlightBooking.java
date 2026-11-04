package Bai8;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * BÀI 8 - Interface từ xa: hệ thống đặt vé máy bay
 */
public interface FlightBooking extends Remote {

    // Đặt "seats" ghế cho chuyến bay, true nếu đặt được
    boolean bookTicket(String flightNumber, int seats) throws RemoteException;

    // Số ghế còn trống, trả về -1 nếu không có chuyến bay này
    int availableSeats(String flightNumber) throws RemoteException;

    // Hủy "seats" ghế đã đặt, true nếu hủy được
    boolean cancelBooking(String flightNumber, int seats) throws RemoteException;

    // (Bổ sung) Xem danh sách các chuyến bay
    String listFlights() throws RemoteException;
}
