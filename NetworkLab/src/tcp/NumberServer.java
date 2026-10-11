package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NumberServer {
    private static final int PORT = 5000;
    
    // Mảng chữ tương ứng từ 0 đến 9
    private static final String[] DIGITS = {
        "Không", "Một", "Hai", "Ba", "Bốn", 
        "Năm", "Sáu", "Bảy", "Tám", "Chín"
    };

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Number Server dang lang nghe tren cong " + PORT + "...");

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

    // Hàm xử lý validation và đổi chữ số thành chữ
    private static String process(String request) {
        String trimmed = request.trim();
        
        // 1. Kiểm tra lệnh thoát
        if (trimmed.equalsIgnoreCase("QUIT")) {
            return "OK BYE";
        }

        // 2. Validation: kiểm tra chuỗi rỗng
        if (trimmed.isEmpty()) {
            return "ERR Yeu cau nhap mot chu so";
        }

        // 3. Validation: kiểm tra xem có đúng là 1 chữ số (0 - 9) hay không
        try {
            int num = Integer.parseInt(trimmed);
            if (num >= 0 && num <= 9) {
                return "OK " + DIGITS[num];
            } else {
                return "ERR Chi chap nhan chu so tu 0 den 9";
            }
        } catch (NumberFormatException e) {
            return "ERR Dinh dang khong hop le, vui long chi nhap so";
        }
    }
}