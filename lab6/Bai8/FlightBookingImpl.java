package Bai8;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BÀI 8 - Server quản lý danh sách chuyến bay
 *
 * Lưu bằng Map<String, Flight>: key = mã chuyến bay (viết hoa), value = đối tượng Flight
 * Dùng LinkedHashMap để khi in ra giữ đúng thứ tự thêm vào.
 */
public class FlightBookingImpl extends UnicastRemoteObject implements FlightBooking {

    private final Map<String, Flight> flights = new LinkedHashMap<>();

    public FlightBookingImpl() throws RemoteException {
        super();
        // Tạo sẵn vài chuyến bay
        themChuyenBay(new Flight("VN123", "Ha Noi - TP.HCM", 100));
        themChuyenBay(new Flight("VJ456", "TP.HCM - Da Nang", 50));
        themChuyenBay(new Flight("QH789", "Ha Noi - Phu Quoc", 30));
    }

    private void themChuyenBay(Flight f) {
        flights.put(f.getFlightNumber().toUpperCase(), f);
    }

    // Tìm chuyến bay theo mã (không phân biệt hoa thường)
    private Flight timChuyenBay(String flightNumber) {
        if (flightNumber == null) return null;
        return flights.get(flightNumber.trim().toUpperCase());
    }

    @Override
    public synchronized boolean bookTicket(String flightNumber, int seats) throws RemoteException {
        Flight f = timChuyenBay(flightNumber);
        boolean ok = (f != null) && f.book(seats);
        System.out.println("[Server] Dat " + seats + " ghe chuyen " + flightNumber + " -> " + (ok ? "thanh cong" : "that bai"));
        return ok;
    }

    @Override
    public synchronized int availableSeats(String flightNumber) throws RemoteException {
        Flight f = timChuyenBay(flightNumber);
        int conTrong = (f == null) ? -1 : f.getAvailableSeats();
        System.out.println("[Server] Kiem tra ghe trong chuyen " + flightNumber + " -> " + conTrong);
        return conTrong;
    }

    @Override
    public synchronized boolean cancelBooking(String flightNumber, int seats) throws RemoteException {
        Flight f = timChuyenBay(flightNumber);
        boolean ok = (f != null) && f.cancel(seats);
        System.out.println("[Server] Huy " + seats + " ghe chuyen " + flightNumber + " -> " + (ok ? "thanh cong" : "that bai"));
        return ok;
    }

    @Override
    public synchronized String listFlights() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        for (Flight f : flights.values()) {
            sb.append(f.toString()).append("\n");
        }
        return sb.toString();
    }
}
