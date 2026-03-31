package mapping;
import java.util.HashMap;
import java.util.Map;

public class Config {

    public static final Map<String, String> MASTER_MAPPING = new HashMap<>();

    static {
        MASTER_MAPPING.put("firstname", "firstname");
        MASTER_MAPPING.put("first name", "firstname");

        MASTER_MAPPING.put("lastname", "lastname");
        MASTER_MAPPING.put("last name", "lastname");

        MASTER_MAPPING.put("email", "email");
        MASTER_MAPPING.put("email address", "email");
        MASTER_MAPPING.put("work email", "email");

        MASTER_MAPPING.put("phone", "phone");
        MASTER_MAPPING.put("mobile", "mobile");
        MASTER_MAPPING.put("mobile number", "mobile");

        MASTER_MAPPING.put("job title", "title");
        MASTER_MAPPING.put("designation", "title");

        MASTER_MAPPING.put("company", "company");
        MASTER_MAPPING.put("company name", "company");
        MASTER_MAPPING.put("organization", "company");

        MASTER_MAPPING.put("city", "city");
        MASTER_MAPPING.put("state", "state");

        MASTER_MAPPING.put("zip", "zip");
        MASTER_MAPPING.put("postal code", "zip");

        MASTER_MAPPING.put("country", "country");
        MASTER_MAPPING.put("linkedin", "linkedin");

        MASTER_MAPPING.put("Job Title (English)", "title");
        MASTER_MAPPING.put("Phone number (land line, no toll free)", "phone");
        MASTER_MAPPING.put("emailEmail 1", "email");
        MASTER_MAPPING.put("zipZip/Poscode", "zip");
        MASTER_MAPPING.put("NZIP", "zip");
        MASTER_MAPPING.put("LinkedIn/Lead Validation Url", "linkedin");
    }
}