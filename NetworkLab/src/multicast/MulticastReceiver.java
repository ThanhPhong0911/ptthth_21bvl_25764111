package multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class MulticastReceiver {
    private static final String GROUP_ADDRESS = "230.0.0.1";
    private static final int PORT = 8888;

    public static void main(String[] args) {
        String clientName = args.length > 0 ? args[0] : "ThanhVien_" + (int)(Math.random() * 1000);
        System.out.println("=== THIET BI THU THONG BAO: [" + clientName + "] ===");

        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
            SocketAddress groupAddress = new InetSocketAddress(group, PORT);
            
            // Gia nhap vao nhom Multicast
            NetworkInterface netIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            if (netIf != null) {
                socket.joinGroup(groupAddress, netIf);
            } else {
                // Tuong thich voi phien ban Java / may ao co dinh cau hinh localhost
                socket.joinGroup(new InetSocketAddress(group, PORT), null);
            }

            System.out.println("[+] Da gia nhap (joinGroup) thanh cong vao nhom " + GROUP_ADDRESS + ":" + PORT);
            System.out.println("[*] Dang lang nghe thong bao tu tram phat...\n");

            byte[] buffer = new byte[2048];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet); // Cho nhan thong bao multicast

                String receivedMessage = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                System.out.println(">>> [" + clientName + " nhan duoc]: " + receivedMessage);

                if (receivedMessage.contains("Kenh phat da dong!")) {
                    System.out.println("[-] Kenh phat dong, tien hanh roi nhom (leaveGroup)...");
                    if (netIf != null) {
                        socket.leaveGroup(groupAddress, netIf);
                    } else {
                        socket.leaveGroup(new InetSocketAddress(group, PORT), null);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Loi ket noi Multicast: " + e.getMessage());
        }
    }
}