import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ByteStreamDemo {
    public static void main(String[] args) {
        String inputPath = "data/input.txt";
        String outputPath = "data/byte_output.txt";

        try (FileInputStream in = new FileInputStream(inputPath);
             FileOutputStream out = new FileOutputStream(outputPath)) {

            int byteData;
            while ((byteData = in.read()) != -1) {
                out.write(byteData);
            }

            System.out.println("Đã sao chép dữ liệu nhị phân từ " + inputPath + " sang " + outputPath);

        } catch (IOException e) {
            System.out.println("Lỗi khi thao tác với byte stream: " + e.getMessage());
        }
    }
}
