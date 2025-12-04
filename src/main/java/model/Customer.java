package model;

import java.util.Date;

public class Customer {

    private int id;
    private String name;
    private String phoneNumber;
    private String email;
    private Date registrationDate; 
    private int userId; 

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    // ✅ Wrapper for servlet compatibility
    public void setPhone(String phone) {
        this.phoneNumber = phone;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Date getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
