package mapping;

import com.opencsv.CSVReader;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.io.FileReader;
import java.util.*;

public class Mapper {

    public static MappingResult mapCSV(String sourcePath, String targetPath) throws Exception {

        // ===============================
        // READ FILES
        // ===============================
        CSVReader sourceReader = new CSVReader(new FileReader(sourcePath));
        CSVReader targetReader = new CSVReader(new FileReader(targetPath));

        List<String[]> sourceData = sourceReader.readAll();
        List<String[]> targetData = targetReader.readAll();

        String[] sourceCols = sourceData.get(0);
        String[] targetCols = targetData.get(0);

        // ===============================
        // NORMALIZED SOURCE MAP
        // ===============================
        Map<String, Integer> normalizedSourceIndex = new HashMap<>();

        for (int i = 0; i < sourceCols.length; i++) {
            normalizedSourceIndex.put(Utils.normalize(sourceCols[i]), i);
        }

        // ===============================
        // FIX 2: COLUMN MAPPING (ONCE)
        // ===============================
        Map<String, String> columnMapping = new HashMap<>();
        List<String> unmappedColumns = new ArrayList<>();

        for (String targetCol : targetCols) {

            String normTarget = Utils.normalize(targetCol);
            String matchedCol = null;

            // ===============================
            // PREFIX MATCH
            // ===============================
            if (normTarget.startsWith("n") && normTarget.length() > 3) {
                String stripped = normTarget.substring(1);

                if (stripped.equals("lname")) stripped = "lastname";

                if (normalizedSourceIndex.containsKey(stripped)) {
                    matchedCol = sourceCols[normalizedSourceIndex.get(stripped)];
                    System.out.println("Prefix match: " + targetCol + " → " + matchedCol);
                }
            }

            // --------------------------------
            // 1. EXACT MATCH
            // --------------------------------
            if (normalizedSourceIndex.containsKey(normTarget)) {
                matchedCol = sourceCols[normalizedSourceIndex.get(normTarget)];
                System.out.println("✅ Exact match: " + targetCol + " → " + matchedCol);
            }

            // --------------------------------
            // 2. DICTIONARY MATCH
            // --------------------------------
            if (matchedCol == null) {
                for (Map.Entry<String, String> entry : Config.MASTER_MAPPING.entrySet()) {
                    if (Utils.normalize(entry.getKey()).equals(normTarget)) {
                        matchedCol = entry.getValue();
                        System.out.println("📘 Dictionary match: " + targetCol + " → " + matchedCol);
                        break;
                    }
                }
            }

            // --------------------------------
            // 3. FUZZY MATCH
            // --------------------------------
            /* if (matchedCol == null) {

                int bestScore = 0;
                String bestMatch = null;

                for (String sourceCol : sourceCols) {
                    int score = FuzzySearch.ratio(
                            targetCol.toLowerCase(),
                            sourceCol.toLowerCase()
                    );

                    if (score > bestScore) {
                        bestScore = score;
                        bestMatch = sourceCol;
                    }
                }

                if (bestScore > 80) {
                    matchedCol = bestMatch;
                    System.out.println("🔍 Fuzzy match: " + targetCol + " → " + matchedCol + " (" + bestScore + "%)");
                }
            } */

            // --------------------------------
            // 4. LLM MATCH (ONLY ONCE!)
            // --------------------------------
            if (matchedCol == null) {
                System.out.println("🤖 Calling LLM for: " + targetCol);
                try {
                    matchedCol = LLM_helper.mapColumn(targetCol, sourceCols);

                    // ✅ VALIDATE LLM OUTPUT
                    if (matchedCol != null && !Mapper.isValidMapping(targetCol, matchedCol, Arrays.asList(sourceCols))) {
                        System.out.println("🚫 Rejected LLM mapping: " + targetCol + " → " + matchedCol);
                        matchedCol = null;
                    }

                    if (matchedCol != null) {
                        System.out.println("🤖 LLM match: " + targetCol + " → " + matchedCol);
                    }

                } catch (Exception e) {
                    System.out.println("❌ LLM ERROR: " + e.getMessage());
                }
            }


            // STORE RESULT (FINAL DECISION)

            if (matchedCol == null) {
                System.out.println("❌ No match for: " + targetCol);
                unmappedColumns.add(targetCol);
            }
            columnMapping.put(targetCol, matchedCol);
        }

        // ===============================
        // OUTPUT DATA
        // ===============================
        List<String[]> output = new ArrayList<>();
        output.add(targetCols); // header


        System.out.println(" UNMAPPED COLUMNS:");
        System.out.println("===============================");

        for (String col : unmappedColumns) {
            System.out.println(" - " + col);
        }

        System.out.println("Total Unmapped: " + unmappedColumns.size());

        // ===============================
        // PROCESS ROWS (NO MATCHING HERE!)
        // ===============================
        for (int i = 1; i < sourceData.size(); i++) {

            String[] sourceRow = sourceData.get(i);
            String[] newRow = new String[targetCols.length];

            for (int j = 0; j < targetCols.length; j++) {

                String targetCol = targetCols[j];
                String matchedCol = columnMapping.get(targetCol);

                if (matchedCol != null) {

                    int index = Arrays.asList(sourceCols).indexOf(matchedCol);

                    if (index != -1) {
                        newRow[j] = sourceRow[index];
                    } else {
                        newRow[j] = "";
                    }

                } else {
                    newRow[j] = "";
                }
            }

            output.add(newRow);
        }

        return new MappingResult(output, unmappedColumns);
    }

    public static boolean isValidMapping(String target, String source, List<String> sourceColumns ) {

        target = Utils.normalize(target);
        source = Utils.normalize(source);

        if (target.startsWith("n") && target.length() > 3) {
            target = target.substring(1);
        }

        // 🚫 Block clearly wrong mappingsF
        if (target.contains("ip")) return false;

        if (target.contains("website")) {
            return source.contains("website");
        }

        if (target.contains("placement")) return false;


        if (target.contains("fname")) return source.contains("firstname");
        if (target.contains("lname")) return source.contains("lastname");

        if (target.contains("name")) {
            return source.contains("name") || source.contains("firstname") || source.contains("lastname");
        }

        // ✅ ID fields
        if (target.contains("orderid")) return false;

        // ✅ Email
        if (target.contains("email")) {
            return source.contains("email");
        }

        // ✅ Phone / Mobile
        if (target.contains("phone") || target.contains("mobile")) {
            return source.contains("phone") || source.contains("mobile");
        }

        if (target.contains("TITLE")) {
            return source.contains("title");
        }

        // ✅ Company / Account
        if (target.contains("company") || target.contains("account")) {
            return source.contains("company");
        }

        // ✅ Address / Street
        if (target.contains("street") || target.contains("address") || target.contains("adline")) {
            return source.contains("address");
        }

        // Zip / Postal
        if (target.contains("zip") || target.contains("postal") || target.endsWith("zip")) {
            return source.contains("zip");
        }

        // -------------------------------
        // ✅ CUSTOM FIELD STRICT MATCH
        // -------------------------------
        if (target.startsWith("custom")) {

            // must match exactly
            if (!target.equals(source)) return false;

            // must exist in source columns
            for (String col : sourceColumns) {
                if (Utils.normalize(col).equals(source)) {
                    return true;
                }
            }
            return false;
        }


        // ✅ Revenue
        if (target.contains("revenue")) {
            return source.contains("revenue") || source.contains("custom");
        }

        //  Prevent wrong mappings
        if (target.contains("placement")) return false;
        if (target.contains("function") && !source.contains("function")) return false;
        if (target.contains("level") && !source.contains("level")) return false;
        if (target.contains("agent") && !source.contains("name")) return false;

        // ✅ Default: allow
        return true;
    }
}