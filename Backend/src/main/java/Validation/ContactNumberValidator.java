package Validation;

import Model.Appointment;
import DAO.AppointmentDAO;

public class ContactNumberValidator extends AppointmentValidator {
    @Override
    public String validate(Appointment appointment, AppointmentDAO dao) {
        String contact = appointment.getNotes();
        if (contact == null || !contact.matches("^0\\d{9}$")) {
            return "Contact number must be exactly 10 digits and start with 0.";
        }
        if (next != null) {
            return next.validate(appointment, dao);
        }
        return null;
    }
}
