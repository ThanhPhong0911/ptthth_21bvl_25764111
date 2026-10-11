package discovery;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class DiscoveryClient {
    private static final int DISCOVERY_PORT = 9999;
    private static final int TIMEOUT_MS = 3000;

    public static void main(String[] args) {
        System.out.println("=== KHOI DONG CLIENT (CHUA BIET IP/PORT SERVER) ===");

        // --- GIAI ĐOẠN 1: KHÁM PHÁ QUA UDP BROADCAST ---
        System.out.println("[Buoc 1] Phat song goi tin UDP Broadcast de tim kiem Server...");
        ServerEndpoint endpoint = discoverServer();

        if (endpoint == null) {
            System.err.println("[-] Khong tim thay Server nao phan hoi trong mang LAN!");
            return;
        }

        System.out.println("[+] Tim thay Server thanh cong tai: " + endpoint.address.getHostAddress() + ":" + endpoint.tcpPort);

        // --- GIAI ĐOẠN 2: KẾT NỐI VÀ TƯƠNG TÁC QUA TCP ---
        System.out.println("\n[Buoc 2] Tien hanh ket noi TCP toi cong " + endpoint.tcpPort + " vua tim duoc...");
        connectTcpService(endpoint);
    }

    private static ServerEndpoint discoverServer() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_MS);

            // Gói tin truy vấn
            String probe = "DISCOVER_SERVICE_REQUEST";
            byte[] sendData = probe.getBytes(StandardCharsets.UTF_8);

            // Gửi tới localhost hoặc toàn mạng LAN
            InetAddress broadcastAddr = InetAddress.getByName("localhost");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddr, DISCOVERY_PORT);
            socket.send(sendPacket);

            // Chờ phản hồi
            byte[] buffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(receivePacket);
                String response = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();

                if (response.startsWith("SERVICE_OFFER")) {
                    int tcpPort = Integer.parseInt(response.split(":")[1].trim());
                    return new ServerEndpoint(receivePacket.getAddress(), tcpPort);
                }
            } catch (SocketTimeoutException e) {
                System.out.println("[-] Het thoi gian cho (Timeout) tim kiem.");
            }
        } catch (Exception e) {
            System.err.println("Loi UDP Discovery: " + e.getMessage());
        }
        return null;
    }

    private static void connectTcpService(ServerEndpoint endpoint) {
        try (
            Socket socket = new Socket(endpoint.address, endpoint.tcpPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)
        ) {
            // Đọc lời chào từ Server
            System.out.println(in.readLine());
            System.out.println(in.readLine());

            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) break;
                String msg = scanner.nextLine();
                out.println(msg);

                if (msg.trim().equalsIgnoreCase("QUIT")) {
                    System.out.println("Server: " + in.readLine());
                    break;
                }

                String response = in.readLine();
                System.out.println(response);
            }
        } catch (Exception e) {
            System.err.println("Loi ket noi TCP: " + e.getMessage());
        }
    }

    static class ServerEndpoint {
        InetAddress address;
        int tcpPort;

        public ServerEndpoint(InetAddress address, int tcpPort) {
            this.address = address;
            this.tcpPort = tcpPort;
        }
    }
}