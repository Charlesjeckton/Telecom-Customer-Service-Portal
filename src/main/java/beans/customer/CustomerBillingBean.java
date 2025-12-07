package beans.customer;

import beans.LoginBean;
import dao.BillingDAO;
import api.mpesa.MpesaService;
import api.mpesa.StkPushResponse;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import model.Billing;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("customerBillingBean")
@ViewScoped
public class CustomerBillingBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private BillingDAO billingDAO;

    @Inject
    private MpesaService mpesaService;

    private List<Billing> paidList = new ArrayList<>();
    private List<Billing> unpaidList = new ArrayList<>();

    private String activeTab = "unpaid"; // default tab

    private String message;
    private String messageType; // "success" or "danger"

    private Billing selectedBill;
    private String customerPhone;
    private String checkoutRequestId;

    private boolean paymentInitiated = false;

    // =========================
    // Bean initialization
    // =========================
    @PostConstruct
    public void init() {
        loadData();
        loadFlashMessage();
    }

    // =========================
    // Load flash messages if any
    // =========================
    private void loadFlashMessage() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        String flashMsg = (String) ec.getFlash().get("customerMessage");
        String flashType = (String) ec.getFlash().get("customerMessageType");

        if (flashMsg != null) {
            this.message = flashMsg;
            this.messageType = flashType != null ? flashType : "success";
        }
    }

    // =========================
    // Load bills for logged-in customer
    // =========================
    public void loadData() {
        Integer customerId = getLoggedCustomerId();
        if (customerId == null) {
            paidList.clear();
            unpaidList.clear();
            return;
        }

        paidList = billingDAO.getPaidBillsByCustomer(customerId);
        unpaidList = billingDAO.getUnpaidBillsByCustomer(customerId);
    }

    private Integer getLoggedCustomerId() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) return null;

        LoginBean loginBean = (LoginBean) ctx.getExternalContext()
                .getSessionMap()
                .get("loginBean");

        if (loginBean == null || loginBean.getCustomerId() == 0)
            return null;

        return loginBean.getCustomerId();
    }

    public void switchTab(String tab) {
        setActiveTab(tab);
        loadData();
    }

    // =========================
    // Initiate M-PESA STK push safely
    // =========================
    public String initiateMpesaPayment() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (!ctx.isPostback()) {
            System.out.println("DEBUG: STK PUSH BLOCKED — REFRESH DETECTED");
            return null;
        }

        paymentInitiated = true;

        if (selectedBill == null) {
            return redirectWithFlash("No bill selected for payment.", "danger");
        }

        if (customerPhone == null || customerPhone.trim().isEmpty()) {
            return redirectWithFlash("Please enter a valid phone number.", "danger");
        }

        try {
            int intAmount = (int) Math.ceil(selectedBill.getAmount());
            String amountStr = String.valueOf(intAmount);

            StkPushResponse response = mpesaService.initiateStkPush(
                    customerPhone,
                    amountStr,
                    selectedBill.getServiceName(),
                    "BILL-" + selectedBill.getId()
            );

            if (response == null) {
                return redirectWithFlash("M-Pesa returned an empty or invalid response.", "danger");
            }

            if ("0".equals(response.getResponseCode())) {
                checkoutRequestId = response.getCheckoutRequestID();
                return redirectWithFlash(
                        response.getCustomerMessage() != null
                                ? response.getCustomerMessage()
                                : "STK Push sent successfully. Check your phone.",
                        "success"
                );
            } else {
                return redirectWithFlash(
                        response.getResponseDescription() != null
                                ? response.getResponseDescription()
                                : "Unknown M-Pesa error occurred.",
                        "danger"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            return redirectWithFlash("M-Pesa Error: " + e.getMessage(), "danger");
        }
    }

    // =========================
    // Finalize payment (callback)
    // =========================
    public void finalizePaymentFromCallback(String mpesaCode) {
        if (selectedBill != null) {
            billingDAO.markBillAsPaid(selectedBill.getId());
            loadData();
        }

        paymentInitiated = false;

        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        ec.getFlash().put("customerMessage", "Payment successful! M-Pesa Code: " + mpesaCode);
        ec.getFlash().put("customerMessageType", "success");

        try {
            ec.redirect("billing.xhtml?tab=unpaid");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================
    // Flash message helper
    // =========================
    private String redirectWithFlash(String msg, String type) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        ec.getFlash().put("customerMessage", msg);
        ec.getFlash().put("customerMessageType", type);

        return "billing.xhtml?tab=unpaid&faces-redirect=true";
    }

    // =========================
    // Getters & Setters
    // =========================
    public List<Billing> getPaidList() { return paidList; }
    public List<Billing> getUnpaidList() { return unpaidList; }

    public String getMessage() { return message; }
    public String getMessageType() { return messageType; }

    public String getActiveTab() { return activeTab; }

    // ✅ Writable property for <f:viewParam>
    public void setActiveTab(String activeTab) {
        if ("paid".equals(activeTab) || "unpaid".equals(activeTab)) {
            this.activeTab = activeTab;
        } else {
            this.activeTab = "unpaid";
        }
    }

    public Billing getSelectedBill() { return selectedBill; }
    public void setSelectedBill(Billing selectedBill) { this.selectedBill = selectedBill; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCheckoutRequestId() { return checkoutRequestId; }
}
