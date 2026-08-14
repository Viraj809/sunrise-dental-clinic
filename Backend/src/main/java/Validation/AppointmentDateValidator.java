package Validation;

import Model.Appointment;
import DAO.AppointmentDAO;

import java.time.LocalDate;

public class AppointmentDateValidator extends AppointmentValidator {
    @Override
    public String validate(Appointment appointment, AppointmentDAO dao) {
        LocalDate today = LocalDate.now();
        LocalDate apptDate = LocalDate.parse(appointment.getAppointmentDate());
        if (apptDate.isBefore(today)) {
            return "Appointment date cannot be in the past.";
        }
        if (next != null) {
            return next.validate(appointment, dao);
        }
        return null;
    }
}
