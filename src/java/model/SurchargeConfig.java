package model;

public class SurchargeConfig {
    private int surchargeId;
    private String surchargeType;
    private double feeAmount;

    public SurchargeConfig() {
    }

    public SurchargeConfig(int surchargeId, String surchargeType, double feeAmount) {
        this.surchargeId = surchargeId;
        this.surchargeType = surchargeType;
        this.feeAmount = feeAmount;
    }

    public int getSurchargeId() {
        return surchargeId;
    }

    public void setSurchargeId(int surchargeId) {
        this.surchargeId = surchargeId;
    }

    public String getSurchargeType() {
        return surchargeType;
    }

    public void setSurchargeType(String surchargeType) {
        this.surchargeType = surchargeType;
    }

    public double getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(double feeAmount) {
        this.feeAmount = feeAmount;
    }

}
