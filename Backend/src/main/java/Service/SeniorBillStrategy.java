package service;

import service.BillCalculationStrategy;

public class SeniorBillStrategy
implements BillCalculationStrategy {
    @Override
    public double calculateTreatmentFee(double basePrice, int patientAge) {
        if (patientAge >= 60) {
            return (double)Math.round(basePrice * 0.9 * 100.0) / 100.0;
        }
        return basePrice;
    }
}

