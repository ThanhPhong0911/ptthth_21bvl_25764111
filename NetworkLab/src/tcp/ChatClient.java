package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        try {
            Socket socket = new Socket(host, port);
            System.out.println("Da ket noi den server chat (" + host + ":" + port + ")");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

            // Luồng phụ: Luôn lắng nghe tin nhắn broadcast từ server đổ về
            Thread receiveThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Ket noi da dong.");
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Luồng chính: Đọc từ bàn phím và gửi lên server
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                out.println(input);
                if (input.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("Khong the ket noi toi Server: " + e.getMessage());
        }
    }
}