package service;

import service.BillCalculationStrategy;

public class StandardBillStrategy
implements BillCalculationStrategy {
    @Override
    public double calculateTreatmentFee(double basePrice, int patientAge) {
        return basePrice;
    }
}

