package model;

public class CancellationPolicy {
    private int policyId;
    private int thresholdHours;
    private double refundPercentage;
    private boolean isActive;

    public CancellationPolicy() {
    }

    public CancellationPolicy(int policyId, int thresholdHours, double refundPercentage, boolean isActive) {
        this.policyId = policyId;
        this.thresholdHours = thresholdHours;
        this.refundPercentage = refundPercentage;
        this.isActive = isActive;
    }

    public int getPolicyId() {
        return policyId;
    }

    public void setPolicyId(int policyId) {
        this.policyId = policyId;
    }

    public int getThresholdHours() {
        return thresholdHours;
    }

    public void setThresholdHours(int thresholdHours) {
        this.thresholdHours = thresholdHours;
    }

    public double getRefundPercentage() {
        return refundPercentage;
    }

    public void setRefundPercentage(double refundPercentage) {
        this.refundPercentage = refundPercentage;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

}
