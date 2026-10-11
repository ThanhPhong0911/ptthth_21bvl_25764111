package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 5000;
    
    // Đồng bộ danh sách client: CopyOnWriteArrayList an toàn khi duyệt và thêm/xóa đa luồng
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    
    // Thread pool: Tự động quản lý và cấp phát luồng cho từng kết nối
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        System.out.println("Chat Server dang chay tren cong " + PORT + "...");

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Loi server: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    // Broadcast: Gửi tin nhắn đến toàn bộ client còn lại
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientHandler handler) {
        clients.remove(handler);
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                out.println("Nhap ten cua ban de tham gia phong chat:");
                clientName = in.readLine();
                if (clientName == null || clientName.trim().isEmpty()) {
                    clientName = "Client_" + socket.getPort();
                }

                System.out.println("[" + clientName + "] da tham gia.");
                broadcast(">>> [" + clientName + "] da vao phong chat!", this);

                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.trim().equalsIgnoreCase("QUIT")) {
                        out.println("Ban da roi khoi phong chat.");
                        break;
                    }
                    System.out.println("[" + clientName + "]: " + msg);
                    broadcast("[" + clientName + "]: " + msg, this);
                }
            } catch (IOException e) {
                System.out.println("[" + clientName + "] bi mat ket noi.");
            } finally {
                removeClient(this);
                broadcast("<<< [" + clientName + "] da roi phong chat.", this);
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }
    }
}