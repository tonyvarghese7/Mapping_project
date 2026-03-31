package mapping;

import okhttp3.*;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class LLM_helper {

    private static final String URL = "http://localhost:11434/api/generate";

    //  Cache to avoid repeated LLM calls
    private static final Map<String, String> CACHE = new HashMap<>();

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build();

    public static String mapColumn(String targetCol, String[] sourceCols) {


        // 1. CACHE CHECK

        if (CACHE.containsKey(targetCol)) {
            return CACHE.get(targetCol);
        }


        // 2. FILTER USELESS FIELDS

        String lower = targetCol.toLowerCase();

        if (lower.contains("opt") ||
                lower.contains("consent") ||
                lower.contains("profiling") ||
                lower.contains("description") ||
                lower.contains("notes")) ||
                lower.contains("ttactic"){

            return null;
        }


        String prompt =
                "You are a STRICT data mapping engine.\n\n" +

                        "Your job:\n" +
                        "Map the TARGET column to EXACTLY ONE column from the SOURCE list.\n\n" +

                        "TARGET:\n" + targetCol + "\n\n" +

                        "SOURCE COLUMNS:\n" + String.join(", ", sourceCols) + "\n\n" +

                        "STRICT RULES:\n" +
                        "1. Return ONLY one column name from SOURCE list (exact spelling)\n" +
                        "2. DO NOT create new names\n" +
                        "3. DO NOT explain\n" +
                        "4. If unsure → return NONE\n" +
                        "5. NEVER guess based on vague similarity\n" +
                        "6. Abbreviations must match logically:\n" +
                        "   ASSETNAME → asset\n" +
                        "   NFNAME → firstname\n" +
                        "   NLNAME → lastname\n" +
                        "   NZIP → zip\n" +
                        "7. DO NOT map unrelated fields:\n" +
                        "   USSCORE != company \n" +
                        "   area_of_responsibility != company \n" +
                        "   Job Function != title \n" +
                        "   Domain != country\n" +
                        "   Comments != linkedin\n" +
                        "   IP != phone\n" +
                        "   Website != linkedin\n" +
                        "8. Custom fields rule:\n" +
                        "   customX → customX ONLY (same number)\n\n" +

                        "Return ONLY the column name OR NONE.\n\n" +

                        "Answer:";


        // 5. CREATE REQUEST

        JSONObject json = new JSONObject();
        json.put("model", "llama3");
        json.put("prompt", prompt);
        json.put("stream", false);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {

            String res = response.body().string();
            JSONObject obj = new JSONObject(res);

            String output = obj.optString("response", "").trim();


            if (output.isEmpty()) {
                System.out.println("⚠️ Empty LLM response for: " + targetCol);
                return null;
            }

            // CLEAN OUTPUT
            output = output.replace("\"", "").trim();


            // Handle Unicode arrow →
            if (output.contains("→")) {
                output = output.substring(output.lastIndexOf("→") + 1).trim();
            }

            // Handle normal arrow ->
            if (output.contains("->")) {
                output = output.substring(output.lastIndexOf("->") + 2).trim();
            }



            // 7. VALIDATE OUTPUT

            String finalMatch = null;

            for (String col : sourceCols) {
                if (col.equalsIgnoreCase(output)) {
                    finalMatch = col;
                    break;
                }
            }

            if (finalMatch == null) {
                System.out.println("⚠️ LLM gave invalid result for: " + targetCol + " → " + output);
                CACHE.put(targetCol, null);
                return null;
            }


            // 8. CACHE RESULT

            CACHE.put(targetCol, finalMatch);
            return finalMatch;

        } catch (Exception e) {
            System.out.println("❌ LLM ERROR for [" + targetCol + "]: " + e.getMessage());
            return null;
        }
    }
}