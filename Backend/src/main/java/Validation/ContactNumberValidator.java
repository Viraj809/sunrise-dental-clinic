package Validation;

import Model.Appointment;
import DAO.AppointmentDAO;

public class ContactNumberValidator extends AppointmentValidator {
    @Override
    public String validate(Appointment appointment, AppointmentDAO dao) {
        String contact = appointment.getContact();
        if (contact == null || !contact.matches("^0\\d{9}$")) {
            return "Please enter a valid Sri Lankan mobile number (e.g. 0712345678).";
        }
        if (next != null) {
            return next.validate(appointment, dao);
        }
        return null;
    }

    public static String validate(String contact) {
        if (contact == null || contact.trim().isEmpty()) {
            return "Contact number is required.";
        }
        if (!contact.matches("^0\\d{9}$")) {
            return "Please enter a valid Sri Lankan mobile number (e.g. 0712345678).";
        }
        return null;
    }
}
