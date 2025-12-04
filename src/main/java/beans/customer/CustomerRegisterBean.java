package beans.customer;

import dao.CustomerDAO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import model.Customer;
import model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Named("customerRegisterBean")
@RequestScoped
public class CustomerRegisterBean {

    private String name;
    private String email;
    private String phone;
    private String username;
    private String password;

    // -------------------------
    //   REGISTER CUSTOMER
    // -------------------------
    public String registerCustomer() throws IOException {

        // Build Customer object
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhoneNumber(phone);
        customer.setRegistrationDate(new Date());

        // Build User object
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("CUSTOMER");

        // Save in DB
        CustomerDAO dao = new CustomerDAO();
        String result = dao.registerCustomer(customer, user);

        // Redirect with outcome message
        FacesContext ctx = FacesContext.getCurrentInstance();

        if ("SUCCESS".equals(result)) {
            String msg = URLEncoder.encode("Customer Registered Successfully", StandardCharsets.UTF_8);
            ctx.getExternalContext().redirect("register.xhtml?success=" + msg);
        } else {
            String msg = URLEncoder.encode(result, StandardCharsets.UTF_8);
            ctx.getExternalContext().redirect("register.xhtml?error=" + msg);
        }

        return null; // Stop JSF navigation
    }

    // -------------------------
    //   GETTERS AND SETTERS
    // -------------------------
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
