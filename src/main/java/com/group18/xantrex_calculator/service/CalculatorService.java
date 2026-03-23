package com.group18.xantrex_calculator.service;

import org.springframework.stereotype.Service;

import com.group18.xantrex_calculator.model.CalculatorResult;
import com.group18.xantrex_calculator.entity.MpptController;
import com.group18.xantrex_calculator.repository.MpptControllerRepository;
import java.util.List;
import java.util.Optional;

@Service
public class CalculatorService {

    private final MpptControllerRepository controllerRepository;

    public CalculatorService(MpptControllerRepository controllerRepository) {
        this.controllerRepository = controllerRepository;
    }

    public CalculatorResult calculate(double pmax, double voc, double isc, int series, int parallel, int batteryVoltage,
            double tempFactor) {
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
        double chargeVoltage = batteryVoltage == 12 ? 14.7 : batteryVoltage == 24 ? 29.4 : batteryVoltage == 36 ? 44.1 : 58.8;
        return totalPower / chargeVoltage;
    }

    public double shortCircuitCurrent(double isc, int parallel) {
        return isc * parallel;
    }

    public Optional<MpptController> findMatchingController(CalculatorResult result, String batteryBank) {
        List<MpptController> controllers = controllerRepository.findAll();
        MpptController best = null;
        for (MpptController c : controllers) {
            if (c.getBatteryBank() == null || c.getMaxVoc() == null
                    || c.getMaxCurrent() == null || c.getMaxIsc() == null) {
                continue;
            }
            if (c.getBatteryBank().contains(batteryBank)
                    && c.getMaxVoc() >= result.getCorrectedVoc()
                    && c.getMaxCurrent() >= result.getMaxChargeCurrent()
                    && c.getMaxIsc() >= result.getShortCircuitCurrent()) {
                if (best == null || c.getMaxVoc() < best.getMaxVoc()) {
                    best = c;
                }
            }
        }
        return Optional.ofNullable(best);
    }

}
