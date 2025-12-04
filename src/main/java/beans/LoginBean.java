package beans;

import dao.UserDAO;
import dao.SubscriptionDAO;
import model.User;
import util.DBConnectionManager;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;

    private User loggedInUser;
    private int customerId;

    private boolean debug = true;           // Show debug info on page
    private boolean dbConnected = false;    // DB connection status

    private final UserDAO userDAO = new UserDAO();

    // ---------------------------
    // Constructor - test DB connection
    // ---------------------------
    public LoginBean() {
        testDbConnection();
    }

    private void testDbConnection() {
        try (Connection conn = DBConnectionManager.getConnection()) {
            dbConnected = true;
        } catch (SQLException e) {
            dbConnected = false;
        }
    }

    // ---------------------------
    // Login with role-based redirect
    // ---------------------------
    public String login() {
        if (username != null) username = username.trim();
        if (password != null) password = password.trim();

        loggedInUser = userDAO.login(username, password);

        if (loggedInUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Invalid username or password", null));
            return null;
        }

        // Store this bean in session for RoleFilter
        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("loginBean", this);

        // Load customer ID if role is CUSTOMER
        if ("CUSTOMER".equalsIgnoreCase(loggedInUser.getRole())) {
            loadCustomerId();
        }

        // Redirect based on role
        String role = loggedInUser.getRole().toUpperCase();

        switch (role) {
            case "ADMIN":
                return "/admin/dashboard.xhtml?faces-redirect=true";

            case "CUSTOMER":
                return "/customer/home.xhtml?faces-redirect=true";

            default:
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unknown role: " + role, null));
                return null;
        }
    }

    // ---------------------------
    // Load REAL customer ID from DB
    // ---------------------------
    private void loadCustomerId() {
        SubscriptionDAO subDao = new SubscriptionDAO();
        customerId = subDao.getCustomerIdByUserId(loggedInUser.getId());

        System.out.println("DEBUG: Loaded customerId = "
                + customerId + " for userId = " + loggedInUser.getId());
    }

    // ---------------------------
    // Logout
    // ---------------------------
    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    // ---------------------------
    // Getters & Setters
    // ---------------------------
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public User getLoggedInUser() { return loggedInUser; }

    public int getCustomerId() { return customerId; }

    public boolean isDebug() { return debug; }
    public boolean isDbConnected() { return dbConnected; }
}
