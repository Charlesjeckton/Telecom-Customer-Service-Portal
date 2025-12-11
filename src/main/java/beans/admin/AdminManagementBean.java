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

    // Add form
    private String newAdminName, newAdminEmail, newAdminPhone, newAdminUsername, newAdminPassword;

    // Edit form
    private int editAdminId;
    private String editAdminName, editAdminEmail, editAdminPhone, editAdminUsername;
    private String editAdminNewPassword, editAdminConfirmPassword;

    // Live-validation flags (used by XHTML)
    private boolean passwordsMatch = true;
    private boolean editUsernameAvailable = true;
    private boolean editEmailAvailable = true;

    @PostConstruct
    public void init() {
        loadAdmins();
    }

    public void loadAdmins() {
        adminList = adminDAO.getAllAdmins();
    }

    // ---------------- Add admin ----------------
    public void registerAdminAndRedirect() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        boolean hasError = false;

        if (isEmpty(newAdminName)) {
            ctx.addMessage("name", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Full name is required", null));
            hasError = true;
        }
        if (isEmpty(newAdminEmail) || adminDAO.emailExists(newAdminEmail)) {
            ctx.addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email is required or already exists", null));
            hasError = true;
        }
        if (isEmpty(newAdminUsername) || adminDAO.usernameExists(newAdminUsername)) {
            ctx.addMessage("username", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username is required or already exists", null));
            hasError = true;
        }
        if (isEmpty(newAdminPassword)) {
            ctx.addMessage("password", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password is required", null));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        Admin admin = new Admin();
        admin.setName(newAdminName);
        admin.setEmail(newAdminEmail);
        admin.setPhone(newAdminPhone);

        boolean success = adminDAO.createAdmin(admin, newAdminUsername, newAdminPassword);
        if (success) {
            flashSuccess("Admin registered successfully!");
            resetAddFields();
            loadAdmins();
        } else {
            flashError("Failed to register admin.");
        }

        redirectToManagement();
    }

    // ---------------- Load admin into edit form ----------------
    public void loadAdminForEdit(Admin admin) {
        if (admin == null) {
            return;
        }
        editAdminId = admin.getId();
        // fetch fresh from DB in case other fields (username) are out of sync
        Admin fresh = adminDAO.getAdminById(editAdminId);
        if (fresh != null) {
            editAdminName = fresh.getName();
            editAdminEmail = fresh.getEmail();
            editAdminPhone = fresh.getPhone();
            editAdminUsername = fresh.getUsername();
        } else {
            // fallback
            editAdminName = admin.getName();
            editAdminEmail = admin.getEmail();
            editAdminPhone = admin.getPhone();
            editAdminUsername = admin.getUsername();
        }

        editAdminNewPassword = editAdminConfirmPassword = null;

        // reset live flags
        passwordsMatch = true;
        editUsernameAvailable = true;
        editEmailAvailable = true;
    }

    // ---------------- Live validators ----------------
    // Called by f:ajax on keyup for password fields
    public void validatePasswordLive() {
        if (isEmpty(editAdminNewPassword) && isEmpty(editAdminConfirmPassword)) {
            passwordsMatch = true;
            return;
        }
        passwordsMatch = (editAdminNewPassword != null && editAdminNewPassword.equals(editAdminConfirmPassword));
    }

    // Called by f:ajax on username input
    public void validateUsernameLive() {
        if (isEmpty(editAdminUsername)) {
            editUsernameAvailable = false;
            return;
        }
        Admin current = adminDAO.getAdminById(editAdminId);
        if (current != null && editAdminUsername.equals(current.getUsername())) {
            editUsernameAvailable = true; // unchanged -> allowed
            return;
        }
        editUsernameAvailable = !adminDAO.usernameExists(editAdminUsername);
    }

    // Called by f:ajax on email input
    public void validateEmailLive() {
        if (isEmpty(editAdminEmail)) {
            editEmailAvailable = false;
            return;
        }
        Admin current = adminDAO.getAdminById(editAdminId);
        if (current != null && editAdminEmail.equals(current.getEmail())) {
            editEmailAvailable = true;
            return;
        }
        editEmailAvailable = !adminDAO.emailExists(editAdminEmail);
    }

    // ---------------- Update admin ----------------
    public void updateAdmin() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        // server-side password match enforcement
        if (!isEmpty(editAdminNewPassword) && !editAdminNewPassword.equals(editAdminConfirmPassword)) {
            ctx.addMessage("editAdminConfirmPassword", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwords do not match", null));
            flashError("Failed to update: passwords do not match.");
            return;
        }

        // server-side username/email uniqueness check (again)
        Admin current = adminDAO.getAdminById(editAdminId);
        if (current == null) {
            flashError("Failed to update: admin not found.");
            return;
        }

        if (!editAdminUsername.equals(current.getUsername()) && adminDAO.usernameExists(editAdminUsername)) {
            ctx.addMessage("editAdminUsername", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username already exists", null));
            flashError("Failed to update: username already exists.");
            return;
        }

        if (!editAdminEmail.equals(current.getEmail()) && adminDAO.emailExists(editAdminEmail)) {
            ctx.addMessage("editAdminEmail", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email already exists", null));
            flashError("Failed to update: email already exists.");
            return;
        }

        // perform updates
        Admin admin = new Admin();
        admin.setId(editAdminId);
        admin.setName(editAdminName);
        admin.setEmail(editAdminEmail);
        admin.setPhone(editAdminPhone);

        boolean infoUpdated = adminDAO.updateAdmin(admin);
        boolean credsUpdated = adminDAO.updateAdminCredentials(admin, editAdminUsername,
                isEmpty(editAdminNewPassword) ? null : editAdminNewPassword);

        if (infoUpdated && credsUpdated) {
            flashSuccess("Admin updated successfully!");
            resetEditFields();
            loadAdmins();
        } else {
            flashError("Failed to update admin.");
        }
    }

    // ---------------- Delete ----------------
    public void deleteAdmin(int id) {
        boolean success = adminDAO.deleteAdmin(id);
        if (success) {
            flashSuccess("Admin deleted successfully!");
        } else {
            flashError("Failed to delete admin.");
        }
        loadAdmins();
    }

    // ---------------- Helpers ----------------
    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void flashSuccess(String text) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("success", text);
    }

    private void flashError(String text) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("error", text);
    }

    private void redirectToManagement() {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/admin/adminManagement.xhtml?faces-redirect=true");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetAddFields() {
        newAdminName = newAdminEmail = newAdminPhone = newAdminUsername = newAdminPassword = null;
    }

    private void resetEditFields() {
        editAdminId = 0;
        editAdminName = editAdminEmail = editAdminPhone = editAdminUsername = null;
        editAdminNewPassword = editAdminConfirmPassword = null;
        passwordsMatch = true;
        editUsernameAvailable = editEmailAvailable = true;
    }

    // ---------------- Getters / Setters ----------------
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

    public String getEditAdminUsername() {
        return editAdminUsername;
    }

    public void setEditAdminUsername(String editAdminUsername) {
        this.editAdminUsername = editAdminUsername;
    }

    public String getEditAdminNewPassword() {
        return editAdminNewPassword;
    }

    public void setEditAdminNewPassword(String editAdminNewPassword) {
        this.editAdminNewPassword = editAdminNewPassword;
    }

    public String getEditAdminConfirmPassword() {
        return editAdminConfirmPassword;
    }

    public void setEditAdminConfirmPassword(String editAdminConfirmPassword) {
        this.editAdminConfirmPassword = editAdminConfirmPassword;
    }

    public boolean isPasswordsMatch() {
        return passwordsMatch;
    }

    public boolean isEditUsernameAvailable() {
        return editUsernameAvailable;
    }

    public boolean isEditEmailAvailable() {
        return editEmailAvailable;
    }
}
