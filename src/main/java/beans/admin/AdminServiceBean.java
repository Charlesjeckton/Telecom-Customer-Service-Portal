package beans.admin;

import dao.ServiceDAO;
import model.Service;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Named("adminServiceBean")
@ViewScoped
public class AdminServiceBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ServiceDAO dao;

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
    // Load ALL services for admin
    // ============================
    @PostConstruct
    public void init() {
        services = dao.getAllServices();

        // Read "success" parameter from URL and show as message
        Map<String, String> params = FacesContext.getCurrentInstance()
                                        .getExternalContext()
                                        .getRequestParameterMap();
        String success = params.get("success");
        if (success != null && !success.isBlank()) {
            successMessage = success;
        }
    }

    // ============================
    // Load service for editing
    // ============================
    public void loadServiceById() {
        if (id == null) return;

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
    // Add New Service
    // ============================
    public String addService() {
        errorMessage = null;
        successMessage = null;

        if (!validateForm()) return null;

        Service service = new Service();
        service.setName(name);
        service.setDescription(description);
        service.setCharge(charge);
        service.setDurationValue(durationValue);
        service.setDurationUnit(durationUnit);
        service.setActive(true);

        boolean saved = dao.addService(service);

        if (saved) {
            // Redirect to services.xhtml with success message
            return "services.xhtml?faces-redirect=true&success=Service added successfully!";
        } else {
            errorMessage = "Failed to add service.";
            return null;
        }
    }

    // ============================
    // Update Existing Service
    // ============================
    public String updateService() {
        errorMessage = null;

        if (selectedService == null) {
            errorMessage = "Service not found.";
            return null;
        }

        if (!validateForm()) return null;

        selectedService.setName(name);
        selectedService.setDescription(description);
        selectedService.setCharge(charge);
        selectedService.setDurationValue(durationValue);
        selectedService.setDurationUnit(durationUnit);

        boolean updated = dao.updateService(selectedService);

        if (updated) {
            return "services.xhtml?faces-redirect=true&success=Service updated successfully!";
        } else {
            errorMessage = "Unable to update service.";
            return null;
        }
    }

    // ============================
    // Activate or Deactivate
    // ============================
    public void toggleStatus(Integer serviceId) {
        if (serviceId == null) return;

        Service service = dao.getServiceById(serviceId);
        if (service == null) {
            errorMessage = "Service not found.";
            return;
        }

        service.setActive(!service.isActive());
        boolean updated = dao.updateService(service);

        if (updated) {
            services = dao.getAllServices();
            successMessage = service.isActive() ? "Service activated!" : "Service deactivated!";
            errorMessage = null;
        } else {
            errorMessage = "Unable to update service status.";
            successMessage = null;
        }
    }

    // ============================
    // Delete Service
    // ============================
    public void delete(Integer serviceId) {
        if (serviceId == null) return;

        boolean deleted = dao.deleteService(serviceId);
        if (deleted) {
            services = dao.getAllServices();
            successMessage = "Service deleted successfully!";
            errorMessage = null;
        } else {
            errorMessage = "Unable to delete service.";
            successMessage = null;
        }
    }

    // ============================
    // Validation
    // ============================
    private boolean validateForm() {
        if (name == null || name.isBlank()) {
            errorMessage = "Service name is required.";
            return false;
        }
        if (description == null || description.isBlank()) {
            errorMessage = "Description is required.";
            return false;
        }
        if (charge == null || charge <= 0) {
            errorMessage = "Charge must be greater than zero.";
            return false;
        }
        if (durationValue == null || durationValue <= 0) {
            errorMessage = "Duration value must be greater than zero.";
            return false;
        }
        if (durationUnit == null || durationUnit.isBlank()) {
            errorMessage = "Duration unit is required.";
            return false;
        }
        return true;
    }

    // ============================
    // Getters & Setters
    // ============================
    public List<Service> getServices() { return services; }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Service getSelectedService() { return selectedService; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getCharge() { return charge; }
    public void setCharge(Double charge) { this.charge = charge; }
    public Integer getDurationValue() { return durationValue; }
    public void setDurationValue(Integer durationValue) { this.durationValue = durationValue; }
    public String getDurationUnit() { return durationUnit; }
    public void setDurationUnit(String durationUnit) { this.durationUnit = durationUnit; }
    public String getErrorMessage() { return errorMessage; }
    public String getSuccessMessage() { return successMessage; }
}
