package com.group18.xantrex_calculator.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mppt_controllers")
public class MpptController {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double maxVoc;
    private Double maxCurrent;
    private Double maxIsc;
    private String batteryBank;

    public MpptController(String name, Double maxVoc, Double maxCurrent, Double maxIsc, String batteryBank) {
        this.name = name;
        this.maxVoc = maxVoc;
        this.maxCurrent = maxCurrent;
        this.maxIsc = maxIsc;
        this.batteryBank = batteryBank;
    }

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMaxVoc() {
        return maxVoc;
    }

    public void setMaxVoc(Double maxVoc) {
        this.maxVoc = maxVoc;
    }

    public Double getMaxCurrent() {
        return maxCurrent;
    }

    public void setMaxCurrent(Double maxCurrent) {
        this.maxCurrent = maxCurrent;
    }

    public Double getMaxIsc() {
        return maxIsc;
    }

    public void setMaxIsc(Double maxIsc) {
        this.maxIsc = maxIsc;
    }

    public String getBatteryBank() {
        return batteryBank;
    }

    public void setBatteryBank(String batteryBank) {
        this.batteryBank = batteryBank;
    }
}
