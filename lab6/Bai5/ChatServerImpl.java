package Bai5;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * BÀI 5 - Cài đặt phía server của chat 1-1
 *
 * Vì là chat 1-1 nên server chỉ giữ 1 client đang chat.
 */
public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {

    private ChatClientCallback client; // client đang chat với server
    private String clientName = "Client";

    public ChatServerImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized void registerClient(String name, ChatClientCallback client) throws RemoteException {
        this.client = client;
        this.clientName = name;
        System.out.println(">>> " + name + " da vao phong chat. Ban co the go tin nhan de tra loi.");
    }

    @Override
    public void sendMessage(String message) throws RemoteException {
        // Server nhận tin nhắn từ client và in ra màn hình
        System.out.println(clientName + ": " + message);
    }

    @Override
    public synchronized void unregisterClient() throws RemoteException {
        System.out.println(">>> " + clientName + " da roi phong chat.");
        this.client = null;
    }

    /**
     * Hàm này KHÔNG nằm trong interface, chỉ dùng nội bộ ở server:
     * người ngồi ở server gõ tin nhắn -> gọi callback để gửi về client.
     */
    public synchronized void replyToClient(String message) {
        if (client == null) {
            System.out.println("(Chua co client nao ket noi, tin nhan khong duoc gui)");
            return;
        }
        try {
            client.receiveMessage("Server: " + message);
        } catch (RemoteException e) {
            System.out.println("(Khong gui duoc, client da mat ket noi)");
            client = null;
        }
    }
}
