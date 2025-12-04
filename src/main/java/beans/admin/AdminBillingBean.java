package beans.admin;

import dao.BillingDAO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import model.Billing;

import java.io.Serializable;
import java.util.List;

@Named("adminBillingBean")
@SessionScoped
public class AdminBillingBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private BillingDAO billingDAO;

    private List<Billing> paidList;
    private List<Billing> unpaidList;

    private String activeTab = "unpaid";

    // Dynamic alert messages
    private String message;
    private String messageType;  // "success" or "error"
    private String lastAction;   // "markPaid" or "markUnpaid"

    @PostConstruct
    public void init() {
        loadData();
    }

    public void loadData() {
        paidList = billingDAO.getPaidBills();
        unpaidList = billingDAO.getUnpaidBills();
    }

    public void switchTab(String tab) {
        this.activeTab = tab;
    }

    public void markBillUnpaid(Integer billId) {
        if (billId == null) {
            setErrorMessage("Invalid Bill ID.");
            return;
        }

        boolean ok = billingDAO.markBillAsUnpaid(billId);
        if (ok) {
            lastAction = "markUnpaid";
            setSuccessMessage("Bill marked as UNPAID successfully!");
        } else {
            setErrorMessage("Failed to mark bill as UNPAID.");
        }

        loadData();
    }

    public void markBillPaid(Integer billId) {
        if (billId == null) {
            setErrorMessage("Invalid Bill ID.");
            return;
        }

        boolean ok = billingDAO.markBillAsPaid(billId);
        if (ok) {
            lastAction = "markPaid";
            setSuccessMessage("Bill marked as PAID successfully!");
        } else {
            setErrorMessage("Failed to mark bill as PAID.");
        }

        loadData();
    }

    public void setSuccessMessage(String msg) {
        this.message = msg;
        this.messageType = "success";
    }

    public void setErrorMessage(String msg) {
        this.message = msg;
        this.messageType = "error";
    }

    public void clearMessage() {
        this.message = null;
        this.messageType = null;
        this.lastAction = null;
    }

    public List<Billing> getPaidList() {
        return paidList;
    }

    public List<Billing> getUnpaidList() {
        return unpaidList;
    }

    public String getActiveTab() {
        return activeTab;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getLastAction() {
        return lastAction;
    }
}
