package Service;

// Standard billing: full base price
public class StandardBillStrategy implements BillCalculationStrategy {
    @Override
    public double calculateTreatmentFee(double basePrice, int patientAge) {
        return basePrice;
    }
}
