import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPEchoClient {
    public final static int serverPort = 7;

    public static void main(String[] args) {
        try {
            // if (args.length == 0) {  // kiểm tra tham số là địa chỉ của server
            //     System.out.println("Syntax: java UDPClient Hostname");
            //     return;
            // }

            DatagramSocket ds = new DatagramSocket();
            InetAddress server = InetAddress.getByName("localhost");

            while (true) {
                // Đọc chuỗi người dùng gõ từ bàn phím
                InputStreamReader isr = new InputStreamReader(System.in);
                BufferedReader br = new BufferedReader(isr);
                String theString = br.readLine();

                // Đổi chuỗi ra mảng byte
                byte[] data = theString.getBytes();

                // Tạo gói tin gửi đi
                DatagramPacket dp = new DatagramPacket(data, data.length, server, serverPort);
                ds.send(dp);

                // Gói tin nhận về
                byte[] buffer = new byte[6000];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                ds.receive(incoming);

                // Đổi dữ liệu nhận được từ mảng byte sang chuỗi và in ra màn hình
                System.out.println(new String(incoming.getData(), 0, incoming.getLength()));
            }
        } catch (IOException ie) {
            System.out.println(ie);
        }
    }
}