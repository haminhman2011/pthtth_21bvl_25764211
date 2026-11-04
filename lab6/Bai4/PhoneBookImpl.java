package Bai4;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * BÀI 4 - Server cài đặt danh bạ
 *
 * Danh bạ lưu bằng HashMap<String, String>:
 *   key   = tên
 *   value = số điện thoại
 *
 * Các phương thức có "synchronized" vì RMI có thể cho nhiều client gọi cùng lúc
 * (mỗi lời gọi chạy trên 1 luồng riêng), HashMap lại không an toàn đa luồng.
 */
public class PhoneBookImpl extends UnicastRemoteObject implements PhoneBook {

    private final Map<String, String> danhBa = new HashMap<>();

    public PhoneBookImpl() throws RemoteException {
        super();
        // Thêm sẵn vài liên hệ mẫu cho dễ test
        danhBa.put("An", "0901111111");
        danhBa.put("Binh", "0902222222");
    }

    @Override
    public synchronized void addContact(String name, String phone) throws RemoteException {
        danhBa.put(name, phone);
        System.out.println("[Server] Them: " + name + " - " + phone);
    }

    @Override
    public synchronized String findContact(String name) throws RemoteException {
        String phone = danhBa.get(name);
        System.out.println("[Server] Tim: " + name + " -> " + (phone == null ? "khong thay" : phone));
        return phone;
    }

    @Override
    public synchronized boolean deleteContact(String name) throws RemoteException {
        boolean daXoa = danhBa.remove(name) != null;
        System.out.println("[Server] Xoa: " + name + " -> " + (daXoa ? "thanh cong" : "khong co ten nay"));
        return daXoa;
    }

    @Override
    public synchronized boolean updateContact(String name, String newPhone) throws RemoteException {
        if (!danhBa.containsKey(name)) {
            System.out.println("[Server] Sua: " + name + " -> khong co ten nay");
            return false;
        }
        danhBa.put(name, newPhone);
        System.out.println("[Server] Sua: " + name + " -> " + newPhone);
        return true;
    }

    @Override
    public synchronized String getAllContacts() throws RemoteException {
        if (danhBa.isEmpty()) {
            return "(Danh ba trong)";
        }
        StringBuilder sb = new StringBuilder();
        int stt = 1;
        for (Map.Entry<String, String> e : danhBa.entrySet()) {
            sb.append(stt++).append(". ").append(e.getKey()).append(" - ").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }
}
