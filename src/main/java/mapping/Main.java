package mapping;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        String source = "data/salesbox.csv";
        String target = "data/anteriad_template.csv";
        String output = "output/final.csv";

        System.out.println("🚀 Starting mapping...");

        List<String[]> mapped = Mapper.mapCSV(source, target);

        CSVWriter writer = new CSVWriter(new FileWriter(output));
        writer.writeAll(mapped);
        writer.close();

        System.out.println("✅ Mapping completed!");
    }
}