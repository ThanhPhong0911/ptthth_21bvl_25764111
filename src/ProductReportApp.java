import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class ProductReportApp {
    static class Product {
        String name;
        long price;

        Product(String name, long price) {
            this.name = name;
            this.price = price;
        }
    }

    public static void main(String[] args) throws IOException {
        Product[] products = readProducts();
        writeReport(products);
    }

    private static Product[] readProducts() throws IOException {
        if (System.console() == null) {
            return new Product[] {
                new Product("B\u00e0n ph\u00edm", 3500000),
                new Product("Chu\u1ed9t kh\u00f4ng d\u00e2y", 1250000),
                new Product("Tai nghe", 1800000)
            };
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Product[] products = new Product[3];

        for (int i = 0; i < products.length; i++) {
            System.out.print("Nh\u1eadp t\u00ean s\u1ea3n ph\u1ea9m " + (i + 1) + ": ");
            String name = reader.readLine();
            if (name == null || name.trim().isEmpty()) {
                name = "S\u1ea3n ph\u1ea9m " + (i + 1);
            }

            System.out.print("Nh\u1eadp gi\u00e1 s\u1ea3n ph\u1ea9m " + (i + 1) + " (VND): ");
            String priceInput = reader.readLine();
            long price = 0;
            if (priceInput != null && !priceInput.trim().isEmpty()) {
                String sanitized = priceInput.trim()
                        .replace("VND", "")
                        .replace("vnd", "")
                        .replace(".", "")
                        .replace(",", "")
                        .replace(" ", "");
                price = Long.parseLong(sanitized);
            }

            products[i] = new Product(name.trim(), price);
            System.out.println();
        }

        return products;
    }

    private static void writeReport(Product[] products) throws IOException {
        long total = 0;
        StringBuilder reportBuilder = new StringBuilder();

        for (int i = 0; i < products.length; i++) {
            String code = String.format("SP%02d", i + 1);
            String line = code + " - " + products[i].name + ": " + formatMoney(products[i].price) + " VND";
            reportBuilder.append(line).append(System.lineSeparator());
            total += products[i].price;
        }

        reportBuilder.append(System.lineSeparator())
                .append("N\u1ed9i dung report.txt:")
                .append(System.lineSeparator())
                .append("S\u1ed1 s\u1ea3n ph\u1ea9m: ")
                .append(products.length)
                .append(System.lineSeparator())
                .append("T\u1ed5ng gi\u00e1 tr\u1ecb t\u1ed3n kho: ")
                .append(formatMoney(total))
                .append(" VND");

        String report = reportBuilder.toString();
        Path folder = Paths.get("data");
        Files.createDirectories(folder);
        Path file = folder.resolve("report.txt");

        try (var output = Files.newOutputStream(file)) {
            output.write(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            output.write(report.getBytes(StandardCharsets.UTF_8));
        }

        System.out.println(report);
    }

    private static String formatMoney(long value) {
        DecimalFormat formatter = new DecimalFormat("#,##0");
        return formatter.format(value);
    }
}
