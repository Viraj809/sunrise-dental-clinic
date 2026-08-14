package Validation;

import Model.Appointment;
import DAO.AppointmentDAO;

public abstract class AppointmentValidator {
    protected AppointmentValidator next;

    public void setNext(AppointmentValidator next) {
        this.next = next;
    }

    public abstract String validate(Appointment appointment, AppointmentDAO dao);
}
