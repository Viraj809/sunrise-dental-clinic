package Validation;

/**
 * NICValidator – validates Sri Lankan National Identity Card numbers.
 *
 * Supported formats:
 *   - Old format : 9 digits + V/v  (e.g. 904567891V, 904567891v)
 *   - New format : 12 digits       (e.g. 200012345678)
 *
 * Leading/trailing spaces are ignored. Case-insensitive for the trailing V.
 */
public class NICValidator {

    /**
     * @return null if valid, otherwise an error message.
     */
    public static String validate(String nic) {
        if (nic == null || nic.trim().isEmpty()) {
            return "NIC number is required.";
        }
        String trimmed = nic.trim().toUpperCase();
        if (trimmed.matches("^\\d{9}V$")) {
            return null;
        }
        if (trimmed.matches("^\\d{12}$")) {
            return null;
        }
        return "Please enter a valid NIC number (e.g. 904567891V or 200012345678).";
    }
}
