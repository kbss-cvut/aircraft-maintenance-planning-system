package cz.cvut.kbss.textanalysis;

/**
 * Utilities for handling CSV files.
 */
public class CsvUtils {

    private CsvUtils() {
        throw new AssertionError();
    }

    /**
     * Sanitizes string which should be a part of a CSV file.
     * <p>
     * This means that if the string contains commas, it is enclosed in double quotes (") and if it contains double
     * quotes, they are doubled, i.e., "test" becomes ""test"".
     *
     * @param str The string to sanitize
     * @return Sanitized string
     */
    public static String sanitizeString(String str) {
        if (str == null) {
            return "";
        }
        String result = str;
        boolean sanitized = result.contains(",");
        if (result.contains("\"")) {
            sanitized = true;
            result = result.replace("\"", "\"\"");
        }
        return sanitized ? "\"" + result + '\"' : result;
    }
}
