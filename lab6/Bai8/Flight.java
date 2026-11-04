package Bai8;

/**
 * BÀI 8 - Lớp Flight (chuyến bay), chỉ dùng ở server
 */
public class Flight {

    private String flightNumber; // mã chuyến bay, ví dụ VN123
    private String route;        // tuyến bay, ví dụ "Ha Noi - TP.HCM"
    private int totalSeats;      // tổng số ghế
    private int bookedSeats;     // số ghế đã đặt

    public Flight(String flightNumber, String route, int totalSeats) {
        this.flightNumber = flightNumber;
        this.route = route;
        this.totalSeats = totalSeats;
        this.bookedSeats = 0;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getRoute() {
        return route;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public int getAvailableSeats() {
        return totalSeats - bookedSeats;
    }

    // Đặt ghế: chỉ đặt được khi còn đủ ghế trống
    public boolean book(int seats) {
        if (seats <= 0 || seats > getAvailableSeats()) {
            return false;
        }
        bookedSeats += seats;
        return true;
    }

    // Hủy ghế: không được hủy nhiều hơn số ghế đã đặt
    public boolean cancel(int seats) {
        if (seats <= 0 || seats > bookedSeats) {
            return false;
        }
        bookedSeats -= seats;
        return true;
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-20s | Tong: %3d | Da dat: %3d | Con trong: %3d",
                flightNumber, route, totalSeats, bookedSeats, getAvailableSeats());
    }
}
