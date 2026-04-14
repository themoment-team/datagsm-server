package team.themoment.datagsm.common.global.h2;

/**
 * H2 function alias implementations to emulate MySQL JSON functions for stage profile.
 * All methods must be public static so H2 can invoke them via ALIAS registration.
 */
public class H2JsonFunctions {

    private H2JsonFunctions() {}

    /**
     * Emulates MySQL JSON_CONTAINS(json_doc, val_json).
     * val_json is a JSON-encoded string like '"student:read"'.
     *
     * @param jsonDoc the JSON document (e.g., '["admin","student:read"]')
     * @param valJson the JSON-encoded value to search for (e.g., '"student:read"')
     * @return true if the JSON array contains the given value
     */
    public static boolean jsonContains(String jsonDoc, String valJson) {
        if (jsonDoc == null || valJson == null) return false;
        String target;
        if (valJson.startsWith("\"") && valJson.endsWith("\"") && valJson.length() >= 2) {
            target = valJson.substring(1, valJson.length() - 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
        } else {
            target = valJson;
        }
        return jsonDoc.contains("\"" + target + "\"");
    }

    /**
     * Overload for 3-argument form: JSON_CONTAINS(json_doc, val_json, path).
     * The path argument is ignored since H2 in-memory JSON is stored as flat array.
     */
    public static boolean jsonContains(String jsonDoc, String valJson, String path) {
        return jsonContains(jsonDoc, valJson);
    }
}
