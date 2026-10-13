import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * SERVER CHAT NHÓM QUA TCP
 * - Server mở cổng 5000, chờ client kết nối.
 * - Mỗi client được phục vụ bởi 1 luồng (ClientHandler) riêng.
 * - Client nhập tên, chọn phòng, rồi chat với các client cùng phòng.
 */
public class ChatServer {

    public static final int PORT = 5000;

    // Danh sách phòng do server cung cấp
    public static String[] rooms = { "Study", "Game", "TraiNghiem", "AmNhac" };

    // Danh sách tất cả client đang kết nối
    public static ArrayList<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Server dang chay o cong " + PORT + " ...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Chờ client kết nối
                Socket socket = serverSocket.accept();
                System.out.println("Co client ket noi: " + socket.getRemoteSocketAddress());

                // Tạo 1 luồng mới để phục vụ client này
                ClientHandler handler = new ClientHandler(socket);
                addClient(handler);
                handler.start();
            }
        } catch (IOException e) {
            System.out.println("Loi server: " + e.getMessage());
        }
    }

    // Thêm client vào danh sách (synchronized để tránh nhiều luồng sửa cùng lúc)
    public static synchronized void addClient(ClientHandler c) {
        clients.add(c);
    }

    // Xóa client khỏi danh sách
    public static synchronized void removeClient(ClientHandler c) {
        clients.remove(c);
    }

    // Gửi tin nhắn cho tất cả client trong cùng phòng (trừ người gửi)
    public static synchronized void sendToRoom(String room, String message, ClientHandler sender) {
        for (ClientHandler c : clients) {
            if (c != sender && room.equals(c.room)) {
                c.send(message);
            }
        }
    }

    // Đếm số người đang ở trong 1 phòng
    public static synchronized int countInRoom(String room) {
        int count = 0;
        for (ClientHandler c : clients) {
            if (room.equals(c.room)) {
                count++;
            }
        }
        return count;
    }
}

/**
 * Luồng phục vụ cho 1 client.
 */
class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public String name;        // tên client
    public String room = null; // phòng hiện tại (null = chưa vào phòng nào)

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // Gửi 1 dòng tin nhắn tới client này
    public void send(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // B1: Hỏi tên
            send("Nhap ten cua ban:");
            name = in.readLine();
            if (name == null) {
                return; // client ngắt kết nối luôn
            }
            name = name.trim();
            send("Xin chao " + name + "!");
            System.out.println(name + " da dang nhap.");

            boolean running = true;
            while (running) {
                // B2: Cho client chọn phòng
                boolean chosen = chooseRoom();
                if (!chosen) {
                    break; // client gõ /exit hoặc mất kết nối
                }

                // B3: Chat trong phòng
                running = chatInRoom();
            }
        } catch (IOException e) {
            System.out.println("Mat ket noi voi " + name);
        } finally {
            // Dọn dẹp khi client thoát
            leaveRoom();
            ChatServer.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                // bỏ qua
            }
            System.out.println(name + " da ngat ket noi.");
        }
    }

    /**
     * Gửi danh sách phòng và đợi client chọn.
     * Trả về true nếu đã vào phòng, false nếu client muốn thoát.
     */
    private boolean chooseRoom() throws IOException {
        while (true) {
            send("===== DANH SACH PHONG =====");
            for (int i = 0; i < ChatServer.rooms.length; i++) {
                String r = ChatServer.rooms[i];
                send((i + 1) + ". Phong " + r + " (" + ChatServer.countInRoom(r) + " nguoi)");
            }
            send("Nhap so thu tu phong de vao (hoac /exit de thoat):");

            String line = in.readLine();
            if (line == null) {
                return false;
            }
            line = line.trim();

            if (line.equals("/exit")) {
                send("Tam biet " + name + "!");
                return false;
            }

            // Kiểm tra số phòng hợp lệ
            int choice;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                send("Vui long nhap SO!");
                continue;
            }

            if (choice < 1 || choice > ChatServer.rooms.length) {
                send("So phong khong hop le!");
                continue;
            }

            // Vào phòng
            room = ChatServer.rooms[choice - 1];
            send(">> Ban da vao phong " + room + ". Go /quit de roi phong, /exit de thoat.");
            ChatServer.sendToRoom(room, ">> " + name + " da vao phong.", this);
            System.out.println(name + " vao phong " + room);
            return true;
        }
    }

    /**
     * Nhận tin nhắn của client và chuyển cho những người cùng phòng.
     * Trả về true nếu client /quit (quay lại danh sách phòng),
     * false nếu client /exit (ngắt kết nối).
     */
    private boolean chatInRoom() throws IOException {
        while (true) {
            String msg = in.readLine();
            if (msg == null) {
                return false; // mất kết nối
            }
            msg = msg.trim();

            if (msg.equals("/quit")) {
                leaveRoom();
                send(">> Ban da roi phong.");
                return true;
            }

            if (msg.equals("/exit")) {
                leaveRoom();
                send("Tam biet " + name + "!");
                return false;
            }

            // Tin nhắn bình thường -> gửi cho cả phòng
            ChatServer.sendToRoom(room, "[" + room + "] " + name + ": " + msg, this);
        }
    }

    // Rời phòng hiện tại và báo cho mọi người trong phòng
    private void leaveRoom() {
        if (room != null) {
            String oldRoom = room;
            room = null;
            ChatServer.sendToRoom(oldRoom, ">> " + name + " da roi phong.", this);
            System.out.println(name + " roi phong " + oldRoom);
        }
    }
}
