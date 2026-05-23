package packagee;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class Validation {
    private Validation() {
    }

    public static boolean isValidUserId(long id) {
        return id > 0 && String.valueOf(id).length() == 12;
    }

    public static boolean isValidPhone(long phone) {
        return String.valueOf(phone).length() == 10;
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.com$");
    }

    public static boolean isValidLicence(String licence) {
        return licence != null && licence.matches("^L-\\d{10} MTL$");
    }

    public static boolean isValidOffice(String office) {
        return office != null && office.matches("^O-\\d{3}$");
    }

    public static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public static LocalTime parseQuarterHour(String value) {
        try {
            LocalTime time = LocalTime.parse(value);
            int minute = time.getMinute();
            if (minute == 0 || minute == 15 || minute == 30 || minute == 45) {
                return time;
            }
            return null;
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
