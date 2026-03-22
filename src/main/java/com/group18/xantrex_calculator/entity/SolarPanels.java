package com.group18.xantrex_calculator.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "SolarPanels")
public class SolarPanels {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double pmax;
    private double voc;
    private double isc;
    private String imageUrl;

    public SolarPanels() {

    }

    public SolarPanels(String name, double pmax, double voc, double isc, String imageUrl) {
        this.name = name;
        this.pmax = pmax;
        this.voc = voc;
        this.isc = isc;
        this.imageUrl = imageUrl;
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

    public double getPmax() {
        return pmax;
    }

    public void setPmax(double pmax) {
        this.pmax = pmax;
    }

    public double getVoc() {
        return voc;
    }

    public void setVoc(double voc) {
        this.voc = voc;
    }

    public double getIsc() {
        return isc;
    }

    public void setIsc(double isc) {
        this.isc = isc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
