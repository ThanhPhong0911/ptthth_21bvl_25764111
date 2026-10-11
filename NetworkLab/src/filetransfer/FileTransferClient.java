package filetransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Scanner;

public class FileTransferClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== FILE TRANSFER CLIENT ===");
        System.out.print("Nhap duong dan file can gui (hoac go 'sample' de tao file test tu dong): ");
        String path = scanner.nextLine().trim();

        File fileToSend;
        if (path.equalsIgnoreCase("sample") || path.isEmpty()) {
            fileToSend = createSampleTestFile();
        } else {
            fileToSend = new File(path);
        }

        if (!fileToSend.exists() || !fileToSend.isFile()) {
            System.err.println("Loi: File khong ton tai hoac khong hop le!");
            return;
        }

        try {
            // 1. Tinh ma bam SHA-256 cua file truoc khi gui
            System.out.println("\nDang tinh ma SHA-256 cho file: " + fileToSend.getName() + "...");
            String sha256 = calculateSha256(fileToSend);
            System.out.println("Ma SHA-256: " + sha256);

            // 2. Ket noi toi Server
            System.out.println("Dang ket noi toi Server " + DEFAULT_HOST + ":" + DEFAULT_PORT + "...");
            try (Socket socket = new Socket(DEFAULT_HOST, DEFAULT_PORT);
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 DataInputStream dis = new DataInputStream(socket.getInputStream());
                 FileInputStream fis = new FileInputStream(fileToSend)) {

                // 3. Gui Metadata
                dos.writeUTF(fileToSend.getName());
                dos.writeLong(fileToSend.length());
                dos.writeUTF(sha256);
                dos.flush();

                // 4. Gui du lieu Byte Stream
                System.out.println("Dang truyen luong byte len server...");
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalSent = 0;
                long fileLength = fileToSend.length();

                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                    totalSent += bytesRead;
                }
                dos.flush();
                System.out.println("Da gui hoan tat " + totalSent + "/" + fileLength + " bytes.");

                // 5. Nhan xac thuc tu Server
                String serverResponse = dis.readUTF();
                System.out.println("\n[PHAN HOI TU SERVER]: " + serverResponse);
            }

        } catch (Exception e) {
            System.err.println("Loi trong qua trinh truyen file: " + e.getMessage());
        }
    }

    private static File createSampleTestFile() {
        File sample = new File("sample_test_file.txt");
        try (FileWriter writer = new FileWriter(sample)) {
            writer.write("Day la tap tin thu nghiem truyen file an toan qua mang Java Socket.\n");
            writer.write("Noi dung duoc bao ve va xac thuc toan ven bang ma bam SHA-256.\n");
            writer.write("Thoi gian tao: " + System.currentTimeMillis() + "\n");
            System.out.println("Da tao file mau: " + sample.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Khong the tao file mau: " + e.getMessage());
        }
        return sample;
    }

    private static String calculateSha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}