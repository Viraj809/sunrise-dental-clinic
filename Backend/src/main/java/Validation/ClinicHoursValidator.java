package validation;

import dao.AppointmentDAO;
import model.Appointment;
import validation.AppointmentValidator;
import java.time.LocalTime;

public class ClinicHoursValidator
extends AppointmentValidator {
    private static final LocalTime OPEN = LocalTime.of(8, 0);
    private static final LocalTime CLOSE = LocalTime.of(18, 0);

    @Override
    public String validate(Appointment appointment, AppointmentDAO dao) {
        LocalTime time = LocalTime.parse(appointment.getAppointmentTime());
        if (time.isBefore(OPEN) || time.isAfter(CLOSE)) {
            return "Appointment time must be between 08:00 and 18:00.";
        }
        if (this.next != null) {
            return this.next.validate(appointment, dao);
        }
        return null;
    }
}

