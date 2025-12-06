package beans.customer;

import beans.LoginBean;
import dao.BillingDAO;
import api.mpesa.MpesaService;
import api.mpesa.StkPushResponse;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import model.Billing;

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

    private String activeTab = "unpaid";

    private String message;
    private String messageType;

    private Billing selectedBill;
    private String customerPhone;
    private String checkoutRequestId;

    // IMPORTANT: Used to prevent refresh-triggered STK calls
    private boolean paymentInitiated = false;

    @PostConstruct
    public void init() {
        loadData();
    }

    // ===================================================
    // SAFE MESSAGE CLEARING (PAGE REFRESH ONLY)
    // ===================================================
    public void preRender() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        boolean isPostback = ctx.isPostback();
        boolean isAjax = ctx.getPartialViewContext().isAjaxRequest();

        // Clear messages ONLY on full browser refresh
        if (!isPostback && !isAjax && !paymentInitiated) {
            message = null;
            messageType = null;
        }
    }

    // ===================================================
    // LOAD CUSTOMER BILLS
    // ===================================================
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
        this.activeTab = tab;
        loadData();
    }

    // ===================================================
    // INITIATE MPESA STK — SAFE VERSION
    // ===================================================
    public void initiateMpesaPayment() {

        FacesContext ctx = FacesContext.getCurrentInstance();

        // 🔥 IMPORTANT: prevent STK push during refresh
        if (!ctx.isPostback()) {
            System.out.println("DEBUG: STK PUSH BLOCKED — REFRESH DETECTED");
            return;
        }

        paymentInitiated = true; // only true when user clicks button

        if (selectedBill == null) {
            setErrorMessage("No bill selected for payment.");
            return;
        }

        if (customerPhone == null || customerPhone.trim().isEmpty()) {
            setErrorMessage("Please enter a valid phone number.");
            return;
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

            // Protect against HTML / plain string responses
            if (response == null) {
                setErrorMessage("M-Pesa returned an empty or invalid response.");
                return;
            }

            if ("0".equals(response.getResponseCode())) {

                checkoutRequestId = response.getCheckoutRequestID();

                setSuccessMessage(
                        response.getCustomerMessage() != null
                                ? response.getCustomerMessage()
                                : "STK Push sent successfully. Check your phone."
                );

            } else {
                setErrorMessage(
                        response.getResponseDescription() != null
                                ? response.getResponseDescription()
                                : "Unknown M-Pesa error occurred."
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            setErrorMessage("M-Pesa Error: " + e.getMessage());
        }
    }

    // ===================================================
    // CALLBACK
    // ===================================================
    public void finalizePaymentFromCallback(String mpesaCode) {

        if (selectedBill != null) {
            billingDAO.markBillAsPaid(selectedBill.getId());
            loadData();
        }

        paymentInitiated = false; // reset

        setSuccessMessage("Payment successful! M-Pesa Code: " + mpesaCode);
    }

    // ===================================================
    // MESSAGE HANDLING
    // ===================================================
    public void clearMessage() {
        preRender(); // deprecated fallback
    }

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

    // GETTERS & SETTERS
    public List<Billing> getPaidList() { return paidList; }
    public List<Billing> getUnpaidList() { return unpaidList; }
    public String getMessage() { return message; }
    public String getMessageType() { return messageType; }
    public String getActiveTab() { return activeTab; }

    public Billing getSelectedBill() { return selectedBill; }
    public void setSelectedBill(Billing selectedBill) { this.selectedBill = selectedBill; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCheckoutRequestId() { return checkoutRequestId; }
}
