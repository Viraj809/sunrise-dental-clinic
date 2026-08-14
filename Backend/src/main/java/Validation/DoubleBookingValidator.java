package Validation;

import Model.Appointment;
import DAO.AppointmentDAO;

public class DoubleBookingValidator extends AppointmentValidator {
    @Override
    public String validate(Appointment appointment, AppointmentDAO dao) {
        java.util.List<Appointment> existing = dao.findByDentistIdAndDate(
                appointment.getDentistId(),
                appointment.getAppointmentDate()
        );
        for (Appointment a : existing) {
            if (a.getAppointmentTime().equals(appointment.getAppointmentTime())
                    && a.getAppointmentId() != appointment.getAppointmentId()) {
                return "Double booking detected: dentist already has an appointment at this date and time.";
            }
        }
        if (next != null) {
            return next.validate(appointment, dao);
        }
        return null;
    }
}
