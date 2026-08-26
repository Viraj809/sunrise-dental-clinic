package service;

import dao.PatientDAO;
import service.BillCalculationStrategy;
import service.SeniorBillStrategy;
import service.StandardBillStrategy;
import java.time.LocalDate;
import java.time.Period;

public class BillFactory {
    public static BillCalculationStrategy createBill(int patientId, PatientDAO dao) {
        String dob = dao.findById(patientId).getDateOfBirth();
        if (dob == null || dob.isEmpty()) {
            return new StandardBillStrategy();
        }
        int age = Period.between(LocalDate.parse(dob), LocalDate.now()).getYears();
        if (age >= 60) {
            return new SeniorBillStrategy();
        }
        return new StandardBillStrategy();
    }
}

