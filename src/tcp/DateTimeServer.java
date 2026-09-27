package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("DateTime Server dang lang nghe tren cong " + PORT + "...");

            while (true) {
                try {
                    Socket socket = server.accept();
                    serve(socket);
                } catch (IOException e) {
                    System.err.println("Loi phien client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Khong mo duoc server: " + e.getMessage());
        }
    }

    private static void serve(Socket socket) throws IOException {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                String response = process(request);
                out.println(response);
                if (request.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        } finally {
            socket.close();
        }
    }

    // Xử lý menu lệnh ngày giờ
    private static String process(String request) {
        String trimmed = request.trim().toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        // Định dạng hiển thị chuẩn ngày / giờ
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        switch (trimmed) {
            case "DATE":
                return "OK " + now.format(dateFormatter);
            case "TIME":
                return "OK " + now.format(timeFormatter);
            case "FULL":
            case "DATETIME":
                return "OK " + now.format(fullFormatter);
            case "HELP":
                return "OK Menu lenh: DATE (ngay), TIME (gio), FULL (ngay gio), QUIT (thoat)";
            case "QUIT":
                return "OK BYE";
            default:
                return "ERR UNKNOWN_COMMAND (Go HELP de xem danh sach lenh)";
        }
    }
}