package com.group18.xantrex_calculator.service;

import org.springframework.stereotype.Service;

import com.group18.xantrex_calculator.model.CalculatorResult;

@Service
public class CalculatorService {

    public CalculatorResult calculate(double pmax, double voc, double isc, int series, int parallel, int batteryVoltage, double tempFactor) {
        double total = totalPower(pmax, series, parallel);
        double corrVoc = correctedVoc(voc, series, tempFactor);
        double maxCurrent = maxChargeCurrent(total, batteryVoltage);
        double shortCurrent = shortCircuitCurrent(isc, parallel);
        return new CalculatorResult(total, corrVoc, maxCurrent, shortCurrent);
    }

    public double totalPower(double pmax, int series, int parallel) {
        return pmax * series * parallel;
    }

    public double correctedVoc(double voc, int series, double tempFactor) {
        return voc * series * tempFactor;
    }

    public double maxChargeCurrent(double totalPower, int batteryVoltage) {
        double chargeVoltage = (batteryVoltage == 12) ? 14.7 : 29.4;
        return totalPower / chargeVoltage;
    }

    public double shortCircuitCurrent(double isc, int parallel) {
        return isc * parallel;
    }
}
