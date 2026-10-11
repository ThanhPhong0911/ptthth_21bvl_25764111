package logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class LogClient {
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 7000;

        try (
            Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)
        ) {
            // Nhan cau hoi dinh danh tu Server
            String serverMsg = in.readLine();
            System.out.println(serverMsg);

            System.out.print("Ma dinh danh cua ban: ");
            String id = scanner.nextLine();
            out.println(id);

            // Nhan phan hoi chao mung
            System.out.println(in.readLine());

            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine();
                out.println(line);

                if (line.trim().equalsIgnoreCase("QUIT")) {
                    System.out.println(in.readLine());
                    break;
                }

                // Xu ly neu client gui lenh HISTORY
                if (line.trim().equalsIgnoreCase("HISTORY")) {
                    String logLine;
                    while ((logLine = in.readLine()) != null) {
                        if (logLine.equals("END_HISTORY")) break;
                        System.out.println(logLine);
                    }
                } else {
                    String resp = in.readLine();
                    System.out.println("Server: " + resp);
                }
            }
        } catch (IOException e) {
            System.err.println("Loi ket noi den LogServer: " + e.getMessage());
        }
    }
}