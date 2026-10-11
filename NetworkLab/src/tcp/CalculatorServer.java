package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class CalculatorServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Calculator Server dang hoat dong tren cong " + PORT + "...");

            while (true) {
                try {
                    Socket socket = server.accept();
                    serve(socket);
                } catch (IOException e) {
                    System.err.println("Loi ket noi: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Khong khoi tao duoc server: " + e.getMessage());
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

    // Thiết kế Protocol: <TOÁN TỬ> <SỐ_1> <SỐ_2> hoặc <SỐ_1> <DẤU> <SỐ_2>
    private static String process(String request) {
        String trimmed = request.trim();
        if (trimmed.isEmpty()) {
            return "ERR Yeu cau khong duoc de trong";
        }
        if (trimmed.equalsIgnoreCase("QUIT")) {
            return "OK BYE";
        }

        // Tách chuỗi theo dấu cách
        String[] parts = trimmed.split("\\s+");
        if (parts.length != 3) {
            return "ERR Sai cu phap. Cu phap chuan: <OP> <A> <B> (Vi du: ADD 10 5 hoac 10 + 5)";
        }

        String op;
        double a, b;

        // Hỗ trợ cả 2 dạng: "ADD 10 5" hoặc "10 + 5"
        try {
            if (isNumeric(parts[0])) {
                a = Double.parseDouble(parts[0]);
                op = parts[1].toUpperCase();
                b = Double.parseDouble(parts[2]);
            } else {
                op = parts[0].toUpperCase();
                a = Double.parseDouble(parts[1]);
                b = Double.parseDouble(parts[2]);
            }
        } catch (NumberFormatException e) {
            return "ERR Du lieu dau vao phai la so hop le";
        }

        // Xử lý logic phép toán và bắt lỗi nghiệp vụ
        double result;
        switch (op) {
            case "ADD":
            case "+":
                result = a + b;
                break;
            case "SUB":
            case "-":
                result = a - b;
                break;
            case "MUL":
            case "*":
                result = a * b;
                break;
            case "DIV":
            case "/":
                if (b == 0) {
                    return "ERR Phep tinh khong hop le: Khong the chia cho 0";
                }
                result = a / b;
                break;
            default:
                return "ERR Phep toan khong hop le. Cac toan tu ho tro: ADD (+), SUB (-), MUL (*), DIV (/)";
        }

        // Định dạng kết quả (nếu là số nguyên thì bỏ phần thập phân .0)
        if (result == (long) result) {
            return "OK " + (long) result;
        }
        return "OK " + result;
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}