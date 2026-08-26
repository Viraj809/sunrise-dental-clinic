package validation;

public class NICValidator {
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

