package model;

import java.util.Date;

public class Subscription {

    private int id;
    private int customerId;
    private int serviceId;

    // Display fields for joined queries
    private String customerName;
    private String serviceName;

    // Correct database fields
    private Date purchaseDate;   // maps to subscriptions.purchase_date
    private Date expiryDate;     // maps to subscriptions.expiry_date (nullable)

    private String status;

    // NEW FIELD (Fixes "cannot find symbol setMonthlyPrice")
    private double monthlyPrice;

    // ===== Constructors =====
    public Subscription() {}

    public Subscription(int customerId, int serviceId, Date purchaseDate, Date expiryDate, String status) {
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public Subscription(int customerId, int serviceId, Date purchaseDate, String status) {
        this(customerId, serviceId, purchaseDate, null, status);
    }

    // ===== Getters & Setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public Date getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Date purchaseDate) { this.purchaseDate = purchaseDate; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // ===== NEW GETTER & SETTER FOR monthlyPrice =====
    public double getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(double monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", serviceId=" + serviceId +
                ", customerName='" + customerName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", purchaseDate=" + purchaseDate +
                ", expiryDate=" + expiryDate +
                ", status='" + status + '\'' +
                ", monthlyPrice=" + monthlyPrice +
                '}';
    }
}
