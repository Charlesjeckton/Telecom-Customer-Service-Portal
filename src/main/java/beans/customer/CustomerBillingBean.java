package beans.customer;

import beans.LoginBean;
import dao.BillingDAO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import model.Billing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("customerBillingBean")
@SessionScoped
public class CustomerBillingBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private BillingDAO billingDAO;

    private List<Billing> paidList = new ArrayList<>();
    private List<Billing> unpaidList = new ArrayList<>();

    private String activeTab = "unpaid";

    // Message fields used in UI
    private String message;
    private String messageType;   // success | danger

    @PostConstruct
    public void init() {
        loadData();
    }

    // =============================================
    // LOAD CUSTOMER BILLS
    // =============================================
    public void loadData() {
        Integer customerId = getLoggedCustomerId();

        if (customerId != null) {
            paidList = billingDAO.getPaidBillsByCustomer(customerId);
            unpaidList = billingDAO.getUnpaidBillsByCustomer(customerId);
        } else {
            paidList = new ArrayList<>();
            unpaidList = new ArrayList<>();
        }
    }

    // =============================================
    // GET LOGGED CUSTOMER ID
    // =============================================
    private Integer getLoggedCustomerId() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) return null;

        LoginBean loginBean = (LoginBean)
                ctx.getExternalContext().getSessionMap().get("loginBean");

        if (loginBean == null || loginBean.getCustomerId() == 0) {
            return null;
        }

        return loginBean.getCustomerId();
    }

    // =============================================
    // SWITCH TAB
    // =============================================
    public void switchTab(String tab) {
        this.activeTab = tab;
        loadData();
    }

    // =============================================
    // MARK AS UNPAID
    // =============================================
    public void markBillUnpaid(Integer billId) {
        if (billId == null) {
            setErrorMessage("Invalid Bill ID.");
            return;
        }

        boolean ok = billingDAO.markBillAsUnpaid(billId);

        if (ok) {
            setSuccessMessage("Bill marked as UNPAID successfully!");
        } else {
            setErrorMessage("Failed to update bill status.");
        }

        loadData();
    }

    // =============================================
    // MARK AS PAID
    // =============================================
    public void markBillPaid(Integer billId) {
        if (billId == null) {
            setErrorMessage("Invalid Bill ID.");
            return;
        }

        boolean ok = billingDAO.markBillAsPaid(billId);

        if (ok) {
            setSuccessMessage("Bill marked as PAID successfully!");
        } else {
            setErrorMessage("Failed to update bill status.");
        }

        loadData();
    }

    // =============================================
    // MESSAGE HANDLING
    // =============================================
    private void setMessage(String msg, String type) {
        this.message = msg;
        this.messageType = type;
    }

    public void setSuccessMessage(String msg) {
        setMessage(msg, "success");
    }

    public void setErrorMessage(String msg) {
        setMessage(msg, "danger");
    }

    public void clearMessage() {
        this.message = null;
        this.messageType = null;
    }

    // =============================================
    // GETTERS FOR XHTML
    // =============================================
    public List<Billing> getPaidList() {
        return paidList;
    }

    public List<Billing> getUnpaidList() {
        return unpaidList;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getActiveTab() {
        return activeTab;
    }
}
