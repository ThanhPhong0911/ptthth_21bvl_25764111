package multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class MulticastSender {
    private static final String GROUP_ADDRESS = "230.0.0.1";
    private static final int PORT = 8888;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("=== TRAM PHAT THONG BAO MULTICAST ===");
        System.out.println("Dia chi nhom: " + GROUP_ADDRESS + " | Cong: " + PORT);
        System.out.println("Nhap tin nhan can phat thanh (go 'EXIT' de dung):");

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {

            InetAddress group = InetAddress.getByName(GROUP_ADDRESS);

            while (true) {
                System.out.print("[Phat tin] > ");
                String message = scanner.nextLine().trim();

                if (message.equalsIgnoreCase("EXIT")) {
                    // Gui thong bao dong he thong cho cac thanh vien
                    sendPacket(socket, group, "[THONG BAO HE THONG]: Kenh phat da dong!");
                    System.out.println("Da dong tram phat thanh.");
                    break;
                }

                if (!message.isEmpty()) {
                    String time = LocalTime.now().format(TIME_FMT);
                    String fullMessage = String.format("[%s] [THONG BAO KHAN]: %s", time, message);
                    sendPacket(socket, group, fullMessage);
                    System.out.println("-> Da phat goi tin toi toan bo nhom " + GROUP_ADDRESS);
                }
            }
        } catch (IOException e) {
            System.err.println("Loi khi phat thong bao: " + e.getMessage());
        }
    }

    private static void sendPacket(DatagramSocket socket, InetAddress group, String message) throws IOException {
        byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
        socket.send(packet);
    }
}