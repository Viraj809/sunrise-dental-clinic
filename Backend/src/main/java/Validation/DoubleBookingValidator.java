package validation;

import dao.AppointmentDAO;
import model.Appointment;
import validation.AppointmentValidator;
import java.util.List;

public class DoubleBookingValidator
extends AppointmentValidator {
    @Override
    public String validate(Appointment appointment, AppointmentDAO dao) {
        List<Appointment> existing = dao.findByDentistIdAndDate(appointment.getDentistId(), appointment.getAppointmentDate());
        for (Appointment a : existing) {
            if (!a.getAppointmentTime().equals(appointment.getAppointmentTime()) || a.getAppointmentId() == appointment.getAppointmentId()) continue;
            return "Double booking detected: dentist already has an appointment at this date and time.";
        }
        if (this.next != null) {
            return this.next.validate(appointment, dao);
        }
        return null;
    }
}

