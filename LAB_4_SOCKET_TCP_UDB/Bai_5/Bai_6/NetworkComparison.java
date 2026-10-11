package Bai_5.Bai_6;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;

/** Thuc nghiem echo request/response tren loopback, khong can server ngoai. */
public class NetworkComparison {
    private static final int COUNT = 1000;
    private static final int SIZE = 256;
    private static final int TIMEOUT_MS = 1000;
    private static final InetAddress LOCAL;
    static {
        try { LOCAL = InetAddress.getByName("127.0.0.1"); }
        catch (UnknownHostException e) { throw new ExceptionInInitializerError(e); }
    }

    private static final class Result {
        final String protocol;
        final int run, received;
        final double elapsedMs;
        Result(String protocol, int run, int received, long elapsed) {
            this.protocol = protocol;
            this.run = run;
            this.received = received;
            this.elapsedMs = elapsed / 1_000_000.0;
        }
    }

    public static void main(String[] args) {
        if (args.length > 2) {
            System.err.println("Cách chạy: java Bai_5.Bai_6.NetworkComparison [số_lần>=5] [thư_mục_kết_quả]");
            return;
        }
        try {
            int runs = args.length > 0 ? Integer.parseInt(args[0]) : 5;
            if (runs < 5) throw new IllegalArgumentException("Phải lặp ít nhất 5 lần");
            Path directory = Paths.get(args.length > 1 ? args[1] : "Bai_5/Bai_6");
            Files.createDirectories(directory);
            List<String> report = environment(runs);
            List<Result> results = new ArrayList<>();
            // Warm-up khong dua vao bang ket qua.
            tcp(0);
            udp(0);
            System.out.println("Đã warm-up mỗi giao thức 1 lượt; bắt đầu đo.");
            for (int run = 1; run <= runs; run++) {
                // Dao thu tu de giam anh huong giao thuc luon chay truoc.
                Result first = run % 2 == 1 ? tcp(run) : udp(run);
                Result second = run % 2 == 1 ? udp(run) : tcp(run);
                results.add(first);
                results.add(second);
                for (Result result : Arrays.asList(first, second)) {
                    String line = String.format(Locale.ROOT,
                            "Lần %d | %s | %.3f ms | %d/%d phản hồi", result.run,
                            result.protocol, result.elapsedMs, result.received, COUNT);
                    report.add(line);
                    System.out.println(line);
                }
            }
            report.add("");
            double tcpMedian = summary("TCP", results, report);
            double udpMedian = summary("UDP", results, report);
            report.add(String.format(Locale.ROOT,
                    "Trong đợt đo này, median TCP = %.3f ms, UDP = %.3f ms. %s",
                    tcpMedian, udpMedian, tcpMedian < udpMedian
                            ? "TCP có median thấp hơn." : udpMedian < tcpMedian
                            ? "UDP có median thấp hơn." : "Hai median bằng nhau."));
            report.add("Kết quả chỉ áp dụng cho môi trường và phương pháp hiện tại; không chứng minh UDP luôn nhanh hơn TCP.");
            report.add("Đây là đo echo tuần tự trên localhost, không phải phép đo thông lượng gửi đồng thời hoặc thử mất gói trên mạng thực.");
            report.add("So sánh thời gian phải đi kèm số phản hồi; chạy thiếu phản hồi không có cùng mức hoàn thành công việc.");
            Files.write(directory.resolve("ket_qua.txt"), report, StandardCharsets.UTF_8);
            List<String> csv = new ArrayList<>();
            csv.add("run,protocol,messages,payload_bytes,timeout_ms,received,total_ms");
            results.sort(Comparator.comparingInt((Result r) -> r.run).thenComparing(r -> r.protocol));
            for (Result r : results) csv.add(String.format(Locale.ROOT, "%d,%s,%d,%d,%d,%d,%.6f",
                    r.run, r.protocol, COUNT, SIZE, TIMEOUT_MS, r.received, r.elapsedMs));
            Files.write(directory.resolve("ket_qua.csv"), csv, StandardCharsets.UTF_8);
            System.out.println("Đã lưu ket_qua.txt và ket_qua.csv tại " + directory.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Thực nghiệm không hoàn tất: " + e.getMessage());
            System.exit(1);
        }
    }

    private static byte[] message(int sequence) {
        byte[] data = new byte[SIZE];
        Arrays.fill(data, (byte) 'x');
        ByteBuffer.wrap(data).putInt(sequence);
        return data;
    }

    private static Result tcp(int run) throws Exception {
        ExecutorService worker = Executors.newSingleThreadExecutor();
        try (ServerSocket server = new ServerSocket(0, 1, LOCAL);
             Socket client = new Socket()) {
            Future<?> echo = worker.submit(() -> {
                try (Socket peer = server.accept()) {
                    peer.setTcpNoDelay(true);
                    DataInputStream input = new DataInputStream(peer.getInputStream());
                    OutputStream output = peer.getOutputStream();
                    byte[] buffer = new byte[SIZE];
                    for (int i = 0; i < COUNT; i++) {
                        input.readFully(buffer);
                        output.write(buffer);
                        output.flush();
                    }
                } catch (IOException e) { throw new UncheckedIOException(e); }
            });
            client.connect(new InetSocketAddress(LOCAL, server.getLocalPort()), TIMEOUT_MS);
            client.setTcpNoDelay(true);
            client.setSoTimeout(TIMEOUT_MS);
            DataInputStream input = new DataInputStream(client.getInputStream());
            OutputStream output = client.getOutputStream();
            int received = 0;
            byte[] response = new byte[SIZE];
            long start = System.nanoTime();
            for (int i = 0; i < COUNT; i++) {
                byte[] request = message(i);
                output.write(request);
                output.flush();
                // Mot deadline cho tron ven frame, khong chi cho moi lan read.
                long deadline = System.nanoTime() + TIMEOUT_MS * 1_000_000L;
                int offset = 0;
                while (offset < SIZE) {
                    client.setSoTimeout(remainingMs(deadline));
                    int length = input.read(response, offset, SIZE - offset);
                    if (length < 0) throw new EOFException("TCP server đóng giữa frame");
                    offset += length;
                }
                if (!Arrays.equals(request, response)) throw new IOException("Sai payload TCP");
                received++;
            }
            long elapsed = System.nanoTime() - start;
            echo.get(5, TimeUnit.SECONDS);
            return new Result("TCP", run, received, elapsed);
        } finally { worker.shutdownNow(); }
    }

    private static Result udp(int run) throws Exception {
        ExecutorService worker = Executors.newSingleThreadExecutor();
        try (DatagramSocket server = new DatagramSocket(new InetSocketAddress(LOCAL, 0));
             DatagramSocket client = new DatagramSocket(new InetSocketAddress(LOCAL, 0))) {
            Future<?> echo = worker.submit(() -> {
                try {
                    while (!server.isClosed()) {
                        DatagramPacket packet = new DatagramPacket(new byte[SIZE + 1], SIZE + 1);
                        server.receive(packet);
                        server.send(new DatagramPacket(packet.getData(), packet.getLength(), packet.getAddress(), packet.getPort()));
                    }
                } catch (SocketException e) {
                    if (!server.isClosed()) throw new UncheckedIOException(e);
                } catch (IOException e) { throw new UncheckedIOException(e); }
            });
            client.connect(LOCAL, server.getLocalPort());
            int received = 0;
            long start = System.nanoTime();
            for (int i = 0; i < COUNT; i++) {
                byte[] request = message(i);
                client.send(new DatagramPacket(request, request.length));
                long deadline = System.nanoTime() + TIMEOUT_MS * 1_000_000L;
                try {
                    while (true) {
                        client.setSoTimeout(remainingMs(deadline));
                        DatagramPacket response = new DatagramPacket(new byte[SIZE + 1], SIZE + 1);
                        client.receive(response);
                        if (response.getLength() == SIZE && Arrays.equals(request,
                                Arrays.copyOfRange(response.getData(), response.getOffset(), response.getOffset() + response.getLength()))) {
                            received++;
                            break;
                        }
                        // Bo qua phan hoi tre cua thong diep truoc, khong dem trung.
                    }
                } catch (SocketTimeoutException e) {
                    // Khong gui lai; tiep tuc thong diep tiep theo.
                }
            }
            long elapsed = System.nanoTime() - start;
            server.close();
            echo.get(5, TimeUnit.SECONDS);
            return new Result("UDP", run, received, elapsed);
        } finally { worker.shutdownNow(); }
    }

    private static int remainingMs(long deadline) throws SocketTimeoutException {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0) throw new SocketTimeoutException("Hết deadline phản hồi");
        return (int) Math.max(1, (remaining + 999_999) / 1_000_000);
    }

    private static List<String> environment(int runs) {
        List<String> lines = new ArrayList<>();
        lines.add("BÀI 6 — THỰC NGHIỆM TCP VÀ UDP");
        lines.add("Thời điểm: " + ZonedDateTime.now());
        lines.add("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        lines.add("Java: " + System.getProperty("java.version") + "; JVM: " + System.getProperty("java.vm.name"));
        lines.add("CPU (nếu OS cung cấp): " + System.getenv().getOrDefault("PROCESSOR_IDENTIFIER", "không có"));
        lines.add("Số CPU logic JVM thấy: " + Runtime.getRuntime().availableProcessors());
        lines.add("RAM tối đa JVM (byte): " + Runtime.getRuntime().maxMemory());
        lines.add("Đích: IPv4 loopback 127.0.0.1; server và client cùng một JVM, hai thread, cổng tự cấp.");
        lines.add("Mỗi giao thức: " + runs + " lượt đo x " + COUNT + " thông điệp; payload cố định " + SIZE + " byte mỗi chiều.");
        lines.add("Payload: 4 byte sequence big-endian + 252 byte 'x'; không tính header TCP/UDP/IP.");
        lines.add("Timeout: " + TIMEOUT_MS + " ms cho mỗi phản hồi; TCP connect cũng " + TIMEOUT_MS + " ms.");
        lines.add("TCP: frame cố định 256 byte; đọc đủ frame; TCP_NODELAY bật ở hai đầu.");
        lines.add("UDP: một thông điệp/datagram; xác thực sequence và toàn bộ payload; không gửi lại; bỏ qua phản hồi trễ.");
        lines.add("Phương pháp: stop-and-wait (gửi một, chờ echo rồi gửi tiếp), System.nanoTime đo từ trước gửi thông điệp đầu đến khi nhận/timeout thông điệp cuối.");
        lines.add("Đo bao gồm tạo payload, gửi, chờ, nhận và xác thực; không bao gồm tạo socket, kết nối TCP, đóng socket hoặc ghi báo cáo.");
        lines.add("Warm-up: mỗi giao thức 1 lượt 1000 thông điệp, không tính kết quả; đảo thứ tự TCP/UDP ở các lượt chẵn/lẻ.");
        lines.add("Nếu TCP timeout/EOF/lỗi hoặc server lỗi, dừng thực nghiệm và báo lỗi, không công bố lượt dở dang như kết quả hợp lệ.");
        lines.add("");
        return lines;
    }

    private static double summary(String protocol, List<Result> results, List<String> report) {
        double[] times = results.stream().filter(r -> r.protocol.equals(protocol)).mapToDouble(r -> r.elapsedMs).sorted().toArray();
        int n = times.length;
        double median = n % 2 == 1 ? times[n / 2] : (times[n / 2 - 1] + times[n / 2]) / 2;
        double mean = Arrays.stream(times).average().orElseThrow();
        int received = results.stream().filter(r -> r.protocol.equals(protocol)).mapToInt(r -> r.received).sum();
        report.add(String.format(Locale.ROOT, "%s: mean %.3f ms; median %.3f ms; min %.3f ms; max %.3f ms; phản hồi %d/%d.",
                protocol, mean, median, times[0], times[n - 1], received, n * COUNT));
        return median;
    }
}
