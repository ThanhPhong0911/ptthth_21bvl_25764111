import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BufferedStreamDemo {
    public static void main(String[] args) {
        String inputPath = "data/input.txt";
        String outputPath = "data/buffered_output.txt";

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputPath));
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath))) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }

            System.out.println("Đã sao chép bằng buffered stream đến " + outputPath);

        } catch (IOException e) {
            System.out.println("Lỗi khi thao tác với buffered stream: " + e.getMessage());
        }
    }
}
