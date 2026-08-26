package validation;

import dao.AppointmentDAO;
import model.Appointment;
import validation.AppointmentValidator;
import java.time.LocalDate;

public class AppointmentDateValidator
extends AppointmentValidator {
    @Override
    public String validate(Appointment appointment, AppointmentDAO dao) {
        LocalDate today = LocalDate.now();
        LocalDate apptDate = LocalDate.parse(appointment.getAppointmentDate());
        if (apptDate.isBefore(today)) {
            return "Appointment date cannot be in the past.";
        }
        if (this.next != null) {
            return this.next.validate(appointment, dao);
        }
        return null;
    }
}

