package model;

import java.util.Date;

public class Billing {
    private int id;
    private int customerId;
    private int serviceId;
    private double amount;
    private Date billingDate;   // ✅ Changed from String to Date
    private boolean paid;
    private String serviceName;

    private String customerName;   // ✅ Added
    private String customerEmail;  // ✅ Added

    // ===== Getters and Setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Date getBillingDate() { return billingDate; }
    public void setBillingDate(Date billingDate) { this.billingDate = billingDate; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
}
