package Service;

// Strategy pattern interface: different bill calculation algorithms
public interface BillCalculationStrategy {
    double calculateTreatmentFee(double basePrice, int patientAge);
}
