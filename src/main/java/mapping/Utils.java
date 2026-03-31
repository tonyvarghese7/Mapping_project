package mapping;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    // Normalize function
    public static String normalize(String col) {
        if (col == null) return null;
        return col.toLowerCase()
                  .replace("_", "")
                  .replace(" ", "");
    }

    // Build normalized map
    public static Map<String, String> buildNormalizedMap(List<String> columns) {
        Map<String, String> normalizedMap = new HashMap<>();

        for (String col : columns) {
            normalizedMap.put(normalize(col), col);
        }

        return normalizedMap;
    }
}