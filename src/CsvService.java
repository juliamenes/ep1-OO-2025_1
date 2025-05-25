import java.io.*;
import java.util.List;

public class CsvService {
    private static final String DATA_DIR = "data/";

    static {
        new File(DATA_DIR).mkdirs();
    }

    public static <T> void saveToCsv(String filename, List<T> items, CsvFormatter<T> formatter) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + filename))) {

            writer.println(formatter.getHeader());

            for (T item : items) {
                writer.println(formatter.format(item));
            }
        }
    }

    public interface CsvFormatter<T> {
        String getHeader();

        String format(T item);
    }
}