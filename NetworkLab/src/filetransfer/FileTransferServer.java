package filetransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;

public class FileTransferServer {
    private static final int PORT = 8000;
    private static final String STORAGE_DIR = "server_storage";

    public static void main(String[] args) {
        // Tao thu muc luu file neu chua co
        File dir = new File(STORAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        System.out.println("=== FILE TRANSFER SERVER DANG KHOI DONG ===");
        System.out.println("Lang nghe tren cong: " + PORT);
        System.out.println("Noi luu tru file tai: " + dir.getAbsolutePath());

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\n[+] Co ket noi moi tu: " + clientSocket.getRemoteSocketAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Loi Server: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {
            // 1. Doc Metadata
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            String clientSha256 = dis.readUTF();

            System.out.println("Nhan Metadata -> Ten file: " + fileName + " | Dung luong: " + fileSize + " bytes");
            System.out.println("SHA-256 phia Client: " + clientSha256);

            // 2. Doc Byte Stream va dong thoi tinh SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            File destinationFile = new File(STORAGE_DIR, fileName);

            try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[4096];
                long totalBytesRead = 0;
                int bytesRead;

                while (totalBytesRead < fileSize && 
                      (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    digest.update(buffer, 0, bytesRead); // Cap nhat ma bam theo tung khoi
                    totalBytesRead += bytesRead;
                }
            }

            // 3. Chuyen doi ma bam thanh chuoi Hex
            String serverSha256 = bytesToHex(digest.digest());
            System.out.println("SHA-256 phia Server: " + serverSha256);

            // 4. Kiem tra toan ven
            if (serverSha256.equalsIgnoreCase(clientSha256)) {
                System.out.println("[XAC MINH THANH CONG] File khop 100% voi ban goc!");
                dos.writeUTF("SUCCESS: File da duoc truyen va xac minh toan ven (SHA-256 hop le).");
            } else {
                System.err.println("[CANH BAO] File bi loi hoac can thiep (SHA-256 khong khop)!");
                dos.writeUTF("ERROR: Sai lech ma SHA-256! File co the da bi loi trong qua trinh truyen.");
            }

        } catch (Exception e) {
            System.err.println("Loi xu ly file: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}