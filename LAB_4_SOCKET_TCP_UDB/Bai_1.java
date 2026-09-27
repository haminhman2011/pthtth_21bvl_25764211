import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Bai_1 {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java network.HostInspector <hostname>");
            return;
        }

        try {
            InetAddress[] addresses = InetAddress.getAllByName(args[0]);
            System.out.println("Host: " + args[0]);
            System.out.println("Số địa chỉ trả về: " + addresses.length);

            for (InetAddress address : addresses) {
                System.out.println("- IP: " + address.getHostAddress());
                System.out.println("  Loại địa chỉ: " + getAddressType(address));
                System.out.println("  Canonical: " + address.getCanonicalHostName());
                System.out.println("  Loopback: " + address.isLoopbackAddress());
                System.out.println("  Site local: " + address.isSiteLocalAddress());
            }
        } catch (UnknownHostException e) {
            System.err.println("Không phân giải được host: " + args[0]);
        }
    }

    // Xác định IPv4 hay IPv6 dựa trên kiểu đối tượng
    private static String getAddressType(InetAddress address) {
        if (address instanceof Inet4Address) {
            return "IPv4 (Inet4Address)";
        } else if (address instanceof Inet6Address) {
            return "IPv6 (Inet6Address)";
        }
        return "Không xác định";
    }
}