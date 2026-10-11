package compare;

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

public class CompareServer {
    private static final int TCP_PORT = 6000;
    private static final int UDP_PORT = 6001;

    public static void main(String[] args) {
        System.out.println("=== DICH VU SO SANH TCP & UDP KHOI DONG ===");

        // 1. Luong rieng phuc vu TCP Echo
        Thread tcpThread = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(TCP_PORT)) {
                System.out.println("[TCP Server] Dang lang nghe tren cong " + TCP_PORT + "...");
                while (true) {
                    Socket socket = server.accept();
                    new Thread(() -> handleTcpClient(socket)).start();
                }
            } catch (IOException e) {
                System.err.println("Loi TCP Server: " + e.getMessage());
            }
        });

        // 2. Luong rieng phuc vu UDP Echo
        Thread udpThread = new Thread(() -> {
            try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT)) {
                System.out.println("[UDP Server] Dang lang nghe tren cong " + UDP_PORT + "...");
                byte[] buffer = new byte[1024];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet); // Nhan goi UDP

                    // Gui ngay lap tuc goi tin nguoc lai cho client
                    DatagramPacket responsePacket = new DatagramPacket(
                            packet.getData(),
                            packet.getLength(),
                            packet.getSocketAddress()
                    );
                    udpSocket.send(responsePacket);
                }
            } catch (IOException e) {
                System.err.println("Loi UDP Server: " + e.getMessage());
            }
        });

        tcpThread.start();
        udpThread.start();
    }

    private static void handleTcpClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                out.println("ECHO:" + line);
            }
        } catch (IOException ignored) {
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}