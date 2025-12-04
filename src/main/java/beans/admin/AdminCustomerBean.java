package beans.admin;

import dao.CustomerDAO;
import model.Customer;

import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;

import java.io.Serializable;
import java.util.List;

@Named("adminCustomerBean")
@RequestScoped
public class AdminCustomerBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Customer> customers;

    public AdminCustomerBean() {
        loadCustomers();
    }

    private void loadCustomers() {
        customers = new CustomerDAO().getAllCustomers();
    }

    public List<Customer> getCustomers() {
        return customers;
    }
}
