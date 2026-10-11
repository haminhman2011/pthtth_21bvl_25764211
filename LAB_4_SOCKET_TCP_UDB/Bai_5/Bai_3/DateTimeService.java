package Bai_5.Bai_3;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Xu ly chung cho hai giao thuc, dung mui gio cua may chu. */
final class DateTimeService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MM yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH mm ss");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd MM yyyy HH mm ss");

    private DateTimeService() { }

    static String respond(String command) {
        LocalDateTime now = LocalDateTime.now();
        switch (command) {
            case "DATE": return now.format(DATE);
            case "TIME": return now.format(TIME);
            case "DATETIME": return now.format(DATETIME);
            default: return "ERR UNKNOWN_COMMAND";
        }
    }

    static int port(String[] args, int index, int defaultPort) {
        int port = args.length > index ? Integer.parseInt(args[index]) : defaultPort;
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port phải từ 1 đến 65535");
        }
        return port;
    }
}
