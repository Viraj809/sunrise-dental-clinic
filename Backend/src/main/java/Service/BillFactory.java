package Service;

import DAO.PatientDAO;

// Factory Method: hides which strategy to use based on patient age
public class BillFactory {
    public static BillCalculationStrategy createBill(int patientId, PatientDAO dao) {
        String dob = dao.findById(patientId).getDateOfBirth();
        if (dob == null || dob.isEmpty()) {
            return new StandardBillStrategy();
        }
        int age = java.time.Period.between(
                java.time.LocalDate.parse(dob),
                java.time.LocalDate.now()
        ).getYears();
        if (age >= 60) {
            return new SeniorBillStrategy();
        }
        return new StandardBillStrategy();
    }
}
