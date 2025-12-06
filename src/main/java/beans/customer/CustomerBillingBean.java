package beans.customer;

import beans.LoginBean;
import dao.BillingDAO;

import api.mpesa.MpesaService;
import api.mpesa.StkPushResponse;

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

    @PostConstruct
    public void init() {
        loadData();
    }

    // ===================================================
    // LOAD CUSTOMER BILLS
    // ===================================================
    public void loadData() {
        Integer customerId = getLoggedCustomerId();

        if (customerId == null) {
            paidList = new ArrayList<>();
            unpaidList = new ArrayList<>();
            return;
        }

        paidList = billingDAO.getPaidBillsByCustomer(customerId);
        unpaidList = billingDAO.getUnpaidBillsByCustomer(customerId);
    }

    // ===================================================
    // GET LOGGED CUSTOMER ID
    // ===================================================
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

    // ===================================================
    // SWITCH TAB
    // ===================================================
    public void switchTab(String tab) {
        this.activeTab = tab;
        loadData();
    }

    // ===================================================
    // SELECT BILL
    // ===================================================
    public void selectBill(Billing bill) {
        this.selectedBill = bill;
    }

    // ===================================================
    // INITIATE STK PUSH
    // ===================================================
    public void initiateMpesaPayment() {

        if (selectedBill == null) {
            setErrorMessage("No bill selected for payment.");
            return;
        }

        if (customerPhone == null || customerPhone.trim().isEmpty()) {
            setErrorMessage("Please enter a valid phone number.");
            return;
        }

        try {
            // ===============================
            // FIX: M-Pesa requires integer amount
            // ===============================
            int intAmount = (int) Math.ceil(selectedBill.getAmount());
            String amountStr = String.valueOf(intAmount);

            System.out.println("DEBUG — Sending amount to M-Pesa: " + amountStr);

            // Send STK Push request
            StkPushResponse response = mpesaService.initiateStkPush(
                    customerPhone,
                    amountStr,                             // FIXED
                    selectedBill.getServiceName(),
                    "BILL-" + selectedBill.getId()
            );

            if (response == null) {
                setErrorMessage("M-Pesa returned an empty response.");
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
                                : "M-Pesa reported an unknown error."
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            setErrorMessage("M-Pesa Error: " + e.getMessage());
        }
    }

    // ===================================================
    // CALLBACK HANDLING
    // ===================================================
    public void finalizePaymentFromCallback(String mpesaCode) {

        if (selectedBill != null) {
            billingDAO.markBillAsPaid(selectedBill.getId());
            loadData();
        }

        setSuccessMessage("Payment successful! M-Pesa Code: " + mpesaCode);
    }

    // ===================================================
    // MESSAGE HANDLING
    // ===================================================
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
        if (!FacesContext.getCurrentInstance().isPostback()
                && !FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {

            message = null;
            messageType = null;
        }
    }

    // ===================================================
    // GETTERS & SETTERS
    // ===================================================
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
