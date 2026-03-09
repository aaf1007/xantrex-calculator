package com.group18.xantrex_calculator.model;

public class CalculatorResult {

    private double totalPower;
    private double correctedVoc;
    private double maxChargeCurrent;
    private double shortCircuitCurrent;

    public CalculatorResult(double totalPower, double correctedVoc, double maxChargeCurrent, double shortCircuitCurrent) {
        this.totalPower = totalPower;
        this.correctedVoc = correctedVoc;
        this.maxChargeCurrent = maxChargeCurrent;
        this.shortCircuitCurrent = shortCircuitCurrent;
    }

    public double getTotalPower() {
        return totalPower;
    }

    public double getCorrectedVoc() {
        return correctedVoc;
    }

    public double getMaxChargeCurrent() {
        return maxChargeCurrent;
    }

    public double getShortCircuitCurrent() {
        return shortCircuitCurrent;
    }
}
