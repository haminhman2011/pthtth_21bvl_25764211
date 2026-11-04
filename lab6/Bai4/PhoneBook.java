package Bai4;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * BÀI 4 - Interface từ xa: quản lý danh bạ
 *
 * 3 phương thức theo đề bài: addContact, findContact, deleteContact
 * Em bổ sung thêm updateContact (để "sửa") và getAllContacts (để xem toàn bộ danh bạ).
 */
public interface PhoneBook extends Remote {

    // Thêm liên hệ mới (nếu tên đã có thì ghi đè số mới)
    void addContact(String name, String phone) throws RemoteException;

    // Tìm số điện thoại theo tên, không thấy thì trả về null
    String findContact(String name) throws RemoteException;

    // Xóa liên hệ, trả về true nếu xóa được, false nếu không có tên đó
    boolean deleteContact(String name) throws RemoteException;

    // (Bổ sung) Sửa số điện thoại, trả về false nếu không có tên đó
    boolean updateContact(String name, String newPhone) throws RemoteException;

    // (Bổ sung) Lấy toàn bộ danh bạ dạng chuỗi để in ra
    String getAllContacts() throws RemoteException;
}
