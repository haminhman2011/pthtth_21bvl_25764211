package Bai_5.Bai_1;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/** Bai 1: Kiem tra hostname va cac thanh phan URI tu hai tham so rieng. */
public class HostUriInspector {
    public static void main(String[] args) {
        if (args.length != 2 || args[0].isBlank() || args[1].isBlank()) {
            System.err.println("Lỗi: Cần đúng 2 tham số: hostname và URI.");
            System.err.println("Cách chạy: java Bai_5.bai_1.HostUriInspector <hostname> \"<URI>\"");
            System.exit(1);
        }

        // Kiem tra URI truoc khi phan giai DNS de bao loi cu phap ngay.
        URI uri;
        try {
            uri = new URI(args[1]);
        } catch (URISyntaxException e) {
            System.err.println("Lỗi: URI sai cú pháp: " + e.getReason());
            System.err.println("Vị trí lỗi: " + e.getIndex());
            System.exit(1);
            return;
        }

        System.out.println("=== THÔNG TIN HOST ===");
        System.out.println("Hostname: " + args[0]);
        boolean hostResolved = true;
        try {
            InetAddress[] addresses = InetAddress.getAllByName(args[0]);
            System.out.println("Số địa chỉ IP: " + addresses.length);
            for (InetAddress address : addresses) {
                System.out.println("- IP: " + address.getHostAddress());
                System.out.println("  Loại: " + addressType(address));
                System.out.println("  Loopback: " + address.isLoopbackAddress());
                System.out.println("  Site local: " + address.isSiteLocalAddress());
            }
        } catch (UnknownHostException e) {
            System.err.println("Lỗi: Không phân giải được hostname: " + args[0]);
            hostResolved = false;
        }

        System.out.println("=== THÔNG TIN URI ===");
        System.out.println("URI: " + uri);
        System.out.println("Scheme: " + display(uri.getScheme()));
        System.out.println("Host: " + display(uri.getHost()));
        System.out.println("Port: " + (uri.getPort() == -1 ? "(không chỉ định)" : uri.getPort()));
        System.out.println("Path: " + display(uri.getPath()));
        System.out.println("Query: " + display(uri.getQuery()));
        System.out.println("Fragment: " + display(uri.getFragment()));

        if (!hostResolved) {
            System.exit(1);
        }
    }

    private static String addressType(InetAddress address) {
        if (address instanceof Inet4Address) {
            return "IPv4";
        }
        if (address instanceof Inet6Address) {
            return "IPv6";
        }
        return "Không xác định";
    }

    private static String display(String value) {
        return value == null || value.isEmpty() ? "(không có)" : value;
    }
}
