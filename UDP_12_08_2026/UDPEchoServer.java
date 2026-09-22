import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPEchoServer {
    public final static int serverPort = 7;

    public static void main(String[] args) {
        try {
            DatagramSocket ds = new DatagramSocket(serverPort);
            System.out.println("Server created...");
            byte[] buffer = new byte[6000];

            while (true) {
                // Chờ nhận gói tin gửi đến
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                ds.receive(incoming);

                // Lấy dữ liệu từ gói tin, đổi từ mảng byte ra chuỗi
                String theString = new String(incoming.getData(), 0, incoming.getLength());

                // Tạo gói tin chứa dữ liệu vừa nhận để gửi trả lại
                DatagramPacket outsending = new DatagramPacket(
                        theString.getBytes(),
                        incoming.getLength(),
                        incoming.getAddress(),
                        incoming.getPort());
                ds.send(outsending);
            }
        } catch (IOException ie) {
            System.out.println(ie);
        }
    }
}