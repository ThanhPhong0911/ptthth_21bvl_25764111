package compare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class CompareClient {
    private static final String HOST = "localhost";
    private static final int TCP_PORT = 6000;
    private static final int UDP_PORT = 6001;
    private static final int TOTAL_PACKETS = 100;
    private static final int TIMEOUT_MS = 500; // Timeout 500ms neu goi bi mat

    public static void main(String[] args) throws Exception {
        System.out.println("Bat dau tien trinh danh gia do tre giua TCP va UDP...");
        System.out.println("So luong goi tin thu nghiem: " + TOTAL_PACKETS + " goi/giao thuc\n");

        // 1. Thu nghiem qua TCP
        TestResult tcpResult = testTcp();

        // Nghi ngan 1 giay truoc khi do tiep UDP
        Thread.sleep(1000);

        // 2. Thu nghiem qua UDP
        TestResult udpResult = testUdp();

        // 3. In bang ket qua so sanh
        printReport(tcpResult, udpResult);
    }

    private static TestResult testTcp() {
        System.out.print("[1/2] Dang do luong TCP... ");
        int success = 0;
        long totalRtt = 0;
        long startTime = System.currentTimeMillis();

        try (
            Socket socket = new Socket(HOST, TCP_PORT);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {
            for (int i = 1; i <= TOTAL_PACKETS; i++) {
                long sendTime = System.nanoTime();
                out.println("TEST_PKT_" + i);
                String resp = in.readLine();
                if (resp != null) {
                    long rtt = (System.nanoTime() - sendTime) / 1_000_000; // Doi sang millisecond
                    totalRtt += rtt;
                    success++;
                }
            }
        } catch (IOException e) {
            System.err.println("Loi TCP: " + e.getMessage());
        }

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("Hoan tat!");
        return new TestResult("TCP", TOTAL_PACKETS, success, TOTAL_PACKETS - success, totalTime, (double) totalRtt / success);
    }

    private static TestResult testUdp() {
        System.out.print("[2/2] Dang do luong UDP... ");
        int success = 0;
        int lost = 0;
        long totalRtt = 0;
        long startTime = System.currentTimeMillis();

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS); // Bat co che timeout de bat goi tin bi mat
            InetAddress address = InetAddress.getByName(HOST);
            byte[] receiveBuffer = new byte[1024];

            for (int i = 1; i <= TOTAL_PACKETS; i++) {
                String msg = "TEST_PKT_" + i;
                byte[] sendData = msg.getBytes(StandardCharsets.UTF_8);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, UDP_PORT);

                long sendTime = System.nanoTime();
                socket.send(sendPacket);

                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    socket.receive(receivePacket);
                    long rtt = (System.nanoTime() - sendTime) / 1_000_000;
                    totalRtt += rtt;
                    success++;
                } catch (SocketTimeoutException e) {
                    // Qua thoi gian timeout khong nhan duoc goi -> Tinh la mat goi
                    lost++;
                }
            }
        } catch (IOException e) {
            System.err.println("Loi UDP: " + e.getMessage());
        }

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("Hoan tat!\n");
        double avgRtt = success > 0 ? (double) totalRtt / success : 0;
        return new TestResult("UDP", TOTAL_PACKETS, success, lost, totalTime, avgRtt);
    }

    private static void printReport(TestResult tcp, TestResult udp) {
        System.out.println("---------------- BANG SO SANH HIEN NANG TCP vs UDP ----------------");
        System.out.printf("%-15s | %-12s | %-12s | %-15s | %-12s\n", 
                "Giao thuc", "Goi gui/nhan", "Mat goi (%)", "Tong thoi gian", "RTT trung binh");
        System.out.println("---------------------------------------------------------------------");
        printRow(tcp);
        printRow(udp);
        System.out.println("---------------------------------------------------------------------");
        System.out.println("* Nhan xet:");
        System.out.println("- TCP: Tin cay tuyet doi (0% mat goi), dong thoi dam bao thu tu goi tin.");
        System.out.println("- UDP: Toc do nhanh, overhead thap, nhung de that thoat goi neu bang thong nghen.");
    }

    private static void printRow(TestResult r) {
        double lossRate = ((double) r.lost / r.total) * 100;
        System.out.printf("%-15s | %-12s | %-11.1f%% | %-13s | %-10.2f ms\n",
                r.protocol,
                r.received + "/" + r.total,
                lossRate,
                r.totalDurationMs + " ms",
                r.avgRttMs);
    }

    static class TestResult {
        String protocol;
        int total;
        int received;
        int lost;
        long totalDurationMs;
        double avgRttMs;

        public TestResult(String protocol, int total, int received, int lost, long totalDurationMs, double avgRttMs) {
            this.protocol = protocol;
            this.total = total;
            this.received = received;
            this.lost = lost;
            this.totalDurationMs = totalDurationMs;
            this.avgRttMs = avgRttMs;
        }
    }
}