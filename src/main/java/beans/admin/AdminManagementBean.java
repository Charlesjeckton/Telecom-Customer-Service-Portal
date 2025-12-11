package beans.admin;

import dao.AdminDAO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import model.Admin;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Named("adminManagementBean")
@ViewScoped
public class AdminManagementBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private AdminDAO adminDAO;

    private List<Admin> adminList;

    // Add fields
    private String newAdminName;
    private String newAdminEmail;
    private String newAdminPhone;
    private String newAdminUsername;
    private String newAdminPassword;

    // Edit fields
    private int editAdminId;
    private String editAdminName;
    private String editAdminEmail;
    private String editAdminPhone;

    @PostConstruct
    public void init() {
        loadAdmins();
    }

    /** Load admin list from DB */
    public void loadAdmins() {
        adminList = adminDAO.getAllAdmins();
    }

    /** Add a new admin with validation and redirect */
    public void registerAdminAndRedirect() {
        FacesContext context = FacesContext.getCurrentInstance();
        boolean hasError = false;

        // Validation
        if (newAdminName == null || newAdminName.trim().isEmpty()) {
            context.addMessage("name", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Full name is required", null));
            hasError = true;
        }

        if (newAdminEmail == null || newAdminEmail.trim().isEmpty()) {
            context.addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email is required", null));
            hasError = true;
        } else if (adminDAO.emailExists(newAdminEmail)) {
            context.addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email already exists", null));
            hasError = true;
        }

        if (newAdminUsername == null || newAdminUsername.trim().isEmpty()) {
            context.addMessage("username", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username is required", null));
            hasError = true;
        } else if (adminDAO.usernameExists(newAdminUsername)) {
            context.addMessage("username", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username already exists", null));
            hasError = true;
        }

        if (newAdminPassword == null || newAdminPassword.trim().isEmpty()) {
            context.addMessage("password", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password is required", null));
            hasError = true;
        }

        if (hasError) {
            return; // Stay on the page if validation fails
        }

        // Create and save admin
        Admin admin = new Admin();
        admin.setName(newAdminName);
        admin.setEmail(newAdminEmail);
        admin.setPhone(newAdminPhone);

        boolean success = adminDAO.createAdmin(admin, newAdminUsername, newAdminPassword);

        if (success) {
            resetAddFields();
            // Add flash message
            FacesContext.getCurrentInstance().getExternalContext()
                    .getFlash().put("success", "Admin registered successfully!");
        } else {
            FacesContext.getCurrentInstance().getExternalContext()
                    .getFlash().put("error", "Failed to register admin.");
        }

        // Reload admin list so table updates immediately
        loadAdmins();

        // Redirect to admin management page
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/admin/adminManagement.xhtml?faces-redirect=true");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetAddFields() {
        newAdminName = null;
        newAdminEmail = null;
        newAdminPhone = null;
        newAdminUsername = null;
        newAdminPassword = null;
    }

    /** Load admin data into edit form */
    public void loadAdminForEdit(Admin admin) {
        if (admin != null) {
            editAdminId = admin.getId();
            editAdminName = admin.getName();
            editAdminEmail = admin.getEmail();
            editAdminPhone = admin.getPhone();
        }
    }

    /** Update admin */
    public void updateAdmin() {
        Admin admin = new Admin();
        admin.setId(editAdminId);
        admin.setName(editAdminName);
        admin.setEmail(editAdminEmail);
        admin.setPhone(editAdminPhone);

        boolean success = adminDAO.updateAdmin(admin);

        if (success) {
            loadAdmins();
            resetEditFields();
            FacesContext.getCurrentInstance().getExternalContext()
                    .getFlash().put("success", "Admin updated successfully!");
        } else {
            FacesContext.getCurrentInstance().getExternalContext()
                    .getFlash().put("error", "Failed to update admin.");
        }
    }

    private void resetEditFields() {
        editAdminId = 0;
        editAdminName = null;
        editAdminEmail = null;
        editAdminPhone = null;
    }

    /** Delete admin */
    public void deleteAdmin(int id) {
        boolean success = adminDAO.deleteAdmin(id);
        loadAdmins(); // Reload table after delete
        if (success) {
            FacesContext.getCurrentInstance().getExternalContext()
                    .getFlash().put("success", "Admin deleted successfully!");
        } else {
            FacesContext.getCurrentInstance().getExternalContext()
                    .getFlash().put("error", "Failed to delete admin.");
        }
    }

    // ----------------- Getters & Setters -----------------

    public List<Admin> getAdminList() {
        return adminList;
    }

    public String getNewAdminName() {
        return newAdminName;
    }

    public void setNewAdminName(String newAdminName) {
        this.newAdminName = newAdminName;
    }

    public String getNewAdminEmail() {
        return newAdminEmail;
    }

    public void setNewAdminEmail(String newAdminEmail) {
        this.newAdminEmail = newAdminEmail;
    }

    public String getNewAdminPhone() {
        return newAdminPhone;
    }

    public void setNewAdminPhone(String newAdminPhone) {
        this.newAdminPhone = newAdminPhone;
    }

    public String getNewAdminUsername() {
        return newAdminUsername;
    }

    public void setNewAdminUsername(String newAdminUsername) {
        this.newAdminUsername = newAdminUsername;
    }

    public String getNewAdminPassword() {
        return newAdminPassword;
    }

    public void setNewAdminPassword(String newAdminPassword) {
        this.newAdminPassword = newAdminPassword;
    }

    public int getEditAdminId() {
        return editAdminId;
    }

    public void setEditAdminId(int editAdminId) {
        this.editAdminId = editAdminId;
    }

    public String getEditAdminName() {
        return editAdminName;
    }

    public void setEditAdminName(String editAdminName) {
        this.editAdminName = editAdminName;
    }

    public String getEditAdminEmail() {
        return editAdminEmail;
    }

    public void setEditAdminEmail(String editAdminEmail) {
        this.editAdminEmail = editAdminEmail;
    }

    public String getEditAdminPhone() {
        return editAdminPhone;
    }

    public void setEditAdminPhone(String editAdminPhone) {
        this.editAdminPhone = editAdminPhone;
    }
}
