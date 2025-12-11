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

@Named("adminServiceBean")
@ViewScoped
public class AdminServiceBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ServiceDAO dao;

    private List<Service> services;
    private Integer id;                // Used when editing
    private Integer selectedId;        // Used for delete
    private Service selectedService;

    private String name;
    private String description;
    private Double charge;
    private Integer durationValue;
    private String durationUnit;

    private String errorMessage;
    private String successMessage;

    // ============================
    // Load all services
    // ============================
    @PostConstruct
    public void init() {
        services = dao.getAllServices();

        FacesContext fc = FacesContext.getCurrentInstance();
        successMessage = (String) fc.getExternalContext().getFlash().get("successMessage");
    }

    // ============================
    // Load service for editing
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
    // Add new service
    // ============================
    public String addService() {
        clearMessages();

        if (!validateForm()) {
            return null;
        }

        Service service = new Service();
        service.setName(name);
        service.setDescription(description);
        service.setCharge(charge);
        service.setDurationValue(durationValue);
        service.setDurationUnit(durationUnit);
        service.setActive(true);

        boolean saved = dao.addService(service);

        if (saved) {
            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getFlash().put("successMessage", "Service added successfully!");
            return "services.xhtml?faces-redirect=true";
        } else {
            errorMessage = "Failed to add service.";
            return null;
        }
    }

    // ============================
    // Update service
    // ============================
    public String updateService() {
        clearMessages();

        if (selectedService == null) {
            errorMessage = "Service not found.";
            return null;
        }

        if (!validateForm()) {
            return null;
        }

        selectedService.setName(name);
        selectedService.setDescription(description);
        selectedService.setCharge(charge);
        selectedService.setDurationValue(durationValue);
        selectedService.setDurationUnit(durationUnit);

        boolean updated = dao.updateService(selectedService);

        if (updated) {
            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getFlash().put("successMessage", "Service updated successfully!");
            return "services.xhtml?faces-redirect=true";
        } else {
            errorMessage = "Unable to update service.";
            return null;
        }
    }

    // ============================
    // Toggle service status
    // ============================
    public void toggleStatus(Integer serviceId) {
        clearMessages();

        if (serviceId == null) {
            return;
        }

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
        } else {
            errorMessage = "Unable to update service status.";
        }
    }

    // ============================
// Delete service with billing check
// ============================
    public void delete(int serviceId) {
        clearMessages();

        // Check if service has billing records
        boolean hasBilling = dao.serviceHasBillingRecords(serviceId);

        if (hasBilling) {
            // Show friendly message if service is linked to billing
            errorMessage = "This service cannot be deleted because it has billing records.";
            return;
        }

        // Attempt to delete service
        boolean deleted = dao.deleteService(serviceId);

        if (deleted) {
            // Reload services list and show success
            services = dao.getAllServices();
            successMessage = "Service deleted successfully!";
        } else {
            errorMessage = "Unable to delete service.";
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
    // Clear messages
    // ============================
    private void clearMessages() {
        errorMessage = null;
        successMessage = null;
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

    public Integer getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Integer selectedId) {
        this.selectedId = selectedId;
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
