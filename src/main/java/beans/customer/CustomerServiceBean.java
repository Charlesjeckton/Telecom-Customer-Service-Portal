package beans.customer;

import dao.ServiceDAO;
import model.Service;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;

import java.io.Serializable;
import java.util.List;

@Named("serviceBean")
@ViewScoped
public class CustomerServiceBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Inject DAO instead of using new ServiceDAO()
    @Inject
    private ServiceDAO dao;

    // List of ACTIVE services shown to customer
    private List<Service> services;

    private Integer id;
    private Service selectedService;

    private String name;
    private String description;
    private Double charge;
    private Integer durationValue;
    private String durationUnit;

    private String errorMessage;
    private String successMessage;

    // ============================
    // Initialization: Load ACTIVE services
    // ============================
    @PostConstruct
    public void init() {
        services = dao.getAllActiveServices();
    }

    // ============================
    // Load service for editing (admin)
    // ============================
    public void loadServiceById() {
        if (id == null) {
            return;
        }

        selectedService = dao.getServiceById(id);

        if (selectedService != null) {
            name = selectedService.getName();
            description = selectedService.getDescription();
            charge = selectedService.getCharge();
            durationValue = selectedService.getDurationValue();
            durationUnit = selectedService.getDurationUnit();
        }
    }

 

    // ============================
    // Getters & Setters
    // ============================
    public List<Service> getServices() {
        return services;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Service getSelectedService() {
        return selectedService;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getCharge() {
        return charge;
    }

    public void setCharge(Double charge) {
        this.charge = charge;
    }

    public Integer getDurationValue() {
        return durationValue;
    }

    public void setDurationValue(Integer durationValue) {
        this.durationValue = durationValue;
    }

    public String getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }
}
