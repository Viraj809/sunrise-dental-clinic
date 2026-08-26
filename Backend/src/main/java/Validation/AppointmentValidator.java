package validation;

import dao.AppointmentDAO;
import model.Appointment;

public abstract class AppointmentValidator {
    protected AppointmentValidator next;

    public void setNext(AppointmentValidator next) {
        this.next = next;
    }

    public abstract String validate(Appointment var1, AppointmentDAO var2);
}

