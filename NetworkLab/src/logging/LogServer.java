package logging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogServer {
    private static final int PORT = 7000;
    private static final String LOG_FILE = "messages.log";
    private static final Object FILE_LOCK = new Object(); // Khoa dong bo ghi file an toan da luong
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("=== LOG SERVER DANG KHOI DONG TREN CONG " + PORT + " ===");
        System.out.println("Nhat ky se duoc luu tai tap tin: " + new File(LOG_FILE).getAbsolutePath());

        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new ClientLoggerHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Loi khoi dong server: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    // Ghi dong nhat ky vao tap tin messages.log (Dong bo tranh race condition)
    public static void appendLog(String clientId, String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String logEntry = String.format("[%s] [%s]: %s", timestamp, clientId, message);

        System.out.println(logEntry); // In ra console server

        synchronized (FILE_LOCK) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                writer.write(logEntry);
                writer.newLine();
            } catch (IOException e) {
                System.err.println("Loi khi ghi tep nhat ky: " + e.getMessage());
            }
        }
    }

    // Doc toan bo lich su nhat ky gui ve cho client
    public static void sendHistory(PrintWriter out) {
        File file = new File(LOG_FILE);
        if (!file.exists()) {
            out.println("LOG_CONTENT: (Chua co ban ghi nhat ky nao)");
            out.println("END_HISTORY");
            return;
        }

        synchronized (FILE_LOCK) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    out.println("LOG: " + line);
                }
            } catch (IOException e) {
                out.println("ERR: Khong the doc tap tin nhat ky.");
            }
        }
        out.println("END_HISTORY");
    }

    static class ClientLoggerHandler implements Runnable {
        private final Socket socket;

        public ClientLoggerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String clientIp = socket.getInetAddress().getHostAddress();
            int clientPort = socket.getPort();
            String defaultId = clientIp + ":" + clientPort;
            String clientId = defaultId;

            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
            ) {
                out.println("CONNECT_OK Nhap ma dinh danh cua ban (hoac de trong de dung ID mac dinh):");
                String inputId = in.readLine();
                if (inputId != null && !inputId.trim().isEmpty()) {
                    clientId = inputId.trim();
                }

                appendLog(clientId, "Da ket noi den he thong nhat ky.");
                out.println("CHAO_MUNG [" + clientId + "]! Cac lenh: HISTORY (xem log), QUIT (thoat), hoac go tin nhan bat ky de ghi log.");

                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.equalsIgnoreCase("QUIT")) {
                        out.println("BYE");
                        break;
                    } else if (line.equalsIgnoreCase("HISTORY")) {
                        sendHistory(out);
                    } else if (!line.isEmpty()) {
                        appendLog(clientId, line);
                        out.println("OK DA_GHI_NHAT_KY");
                    }
                }
            } catch (IOException e) {
                System.out.println("Client [" + clientId + "] da ngat ket noi dot ngot.");
            } finally {
                appendLog(clientId, "Da roi khoi he thong nhat ky.");
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }
    }
}