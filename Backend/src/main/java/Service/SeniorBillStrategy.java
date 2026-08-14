package Service;

// Senior citizen billing: 10% discount for patients 60+
public class SeniorBillStrategy implements BillCalculationStrategy {
    @Override
    public double calculateTreatmentFee(double basePrice, int patientAge) {
        if (patientAge >= 60) {
            return Math.round(basePrice * 0.90 * 100.0) / 100.0;
        }
        return basePrice;
    }
}
