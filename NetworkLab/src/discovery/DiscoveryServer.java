package discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class DiscoveryServer {
    private static final int DISCOVERY_PORT = 9999; // Cổng UDP lắng nghe tìm kiếm
    private static final int SERVICE_TCP_PORT = 9090; // Cổng dịch vụ TCP chính thức

    public static void main(String[] args) {
        System.out.println("=== DISCOVERY & SERVICE SERVER KHOI DONG ===");

        // 1. Chạy tiến trình TCP Service trên cổng 9090
        new Thread(DiscoveryServer::startTcpService).start();

        // 2. Chạy tiến trình UDP Discovery trên cổng 9999
        startUdpDiscovery();
    }

    private static void startUdpDiscovery() {
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            System.out.println("[UDP Discovery] Dang lang nghe broadcast tim kiem tren cong " + DISCOVERY_PORT + "...");
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String request = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).trim();
                System.out.println("[UDP Discovery] Nhan yeu cau: \"" + request + "\" tu " + packet.getSocketAddress());

                if (request.equalsIgnoreCase("DISCOVER_SERVICE_REQUEST")) {
                    // Trả lời lại Client kèm thông tin cổng TCP
                    String response = "SERVICE_OFFER PORT:" + SERVICE_TCP_PORT;
                    byte[] responseData = response.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket responsePacket = new DatagramPacket(
                            responseData,
                            responseData.length,
                            packet.getAddress(),
                            packet.getPort()
                    );
                    socket.send(responsePacket);
                    System.out.println("[UDP Discovery] Da gui thong tin cong TCP cho Client.");
                }
            }
        } catch (IOException e) {
            System.err.println("Loi UDP Discovery: " + e.getMessage());
        }
    }

    private static void startTcpService() {
        try (ServerSocket serverSocket = new ServerSocket(SERVICE_TCP_PORT)) {
            System.out.println("[TCP Service] Dich vu chinh thuc dang chay tren cong " + SERVICE_TCP_PORT + "...");
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> handleTcpClient(client)).start();
            }
        } catch (IOException e) {
            System.err.println("Loi TCP Service: " + e.getMessage());
        }
    }

    private static void handleTcpClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            System.out.println("[TCP Service] [+] Client da ket noi thanh cong: " + socket.getRemoteSocketAddress());
            out.println("CHAO MUNG! Ban da tu dong tim thay va ket noi thanh cong toi Server.");
            out.println("Go thong diep bat ky de giao tiep (hoac 'QUIT' de ngat ket noi):");

            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("QUIT")) {
                    out.println("BYE");
                    break;
                }
                out.println("ECHO_SERVICE: " + line.toUpperCase());
            }
        } catch (IOException ignored) {
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}