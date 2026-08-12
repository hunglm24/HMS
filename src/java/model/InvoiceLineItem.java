package model;

public class InvoiceLineItem {
    private int lineItemId;
    private int invoiceId;
    private String itemType;
    private String description;
    private double amount;

    public InvoiceLineItem() {
    }

    public InvoiceLineItem(int lineItemId, int invoiceId, String itemType, String description, double amount) {
        this.lineItemId = lineItemId;
        this.invoiceId = invoiceId;
        this.itemType = itemType;
        this.description = description;
        this.amount = amount;
    }

    public int getLineItemId() {
        return lineItemId;
    }

    public void setLineItemId(int lineItemId) {
        this.lineItemId = lineItemId;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

}
