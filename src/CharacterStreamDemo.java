import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CharacterStreamDemo {
    public static void main(String[] args) {
        String inputPath = "data/input.txt";
        String outputPath = "data/char_output.txt";

        try (FileReader reader = new FileReader(inputPath);
             FileWriter writer = new FileWriter(outputPath)) {

            int ch;
            while ((ch = reader.read()) != -1) {
                writer.write(ch);
            }

            System.out.println("Đã sao chép dữ liệu ký tự từ " + inputPath + " sang " + outputPath);

        } catch (IOException e) {
            System.out.println("Lỗi khi thao tác với character stream: " + e.getMessage());
        }
    }
}
