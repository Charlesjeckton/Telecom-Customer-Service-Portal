package beans.admin;

import dao.CustomerDAO;
import model.Customer;

import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;
import java.util.Map;

@Named("adminCustomerDetailsBean")
@RequestScoped
public class AdminCustomerDetailsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Customer customer;
    private boolean invalid;
    private boolean notFound;

    public AdminCustomerDetailsBean() {
        loadCustomer();
    }

    private void loadCustomer() {
        Map<String, String> params = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap();

        String idParam = params.get("id");

        if (idParam == null || idParam.isBlank()) {
            invalid = true;
            return;
        }

        int id;

        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException ex) {
            invalid = true;
            return;
        }

        customer = new CustomerDAO().getCustomerById(id);

        if (customer == null) {
            notFound = true;
        }
    }

    // ---------------- Getters ----------------
    public Customer getCustomer() {
        return customer;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public boolean isNotFound() {
        return notFound;
    }
}
