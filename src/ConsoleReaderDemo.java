import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleReaderDemo {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Nhập tên của bạn: ");
        String name = reader.readLine();

        System.out.println("Xin chào, " + name + "!");
    }
}
