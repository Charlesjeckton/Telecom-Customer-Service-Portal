package api.mpesa;

import com.google.gson.annotations.SerializedName;

public class StkPushResponse {

    @SerializedName("MerchantRequestID")
    private String merchantRequestID;

    @SerializedName("CheckoutRequestID")
    private String checkoutRequestID;

    @SerializedName("ResponseCode")
    private String responseCode;

    @SerializedName("ResponseDescription")
    private String responseDescription;

    @SerializedName("CustomerMessage")
    private String customerMessage;

    // ========================================
    // GETTERS
    // ========================================
    public String getMerchantRequestID() {
        return merchantRequestID;
    }

    public String getCheckoutRequestID() {
        return checkoutRequestID;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseDescription() {
        return responseDescription;
    }

    public String getCustomerMessage() {
        return customerMessage;
    }

    // ========================================
    // SETTERS — needed for error handling
    // ========================================
    public void setMerchantRequestID(String merchantRequestID) {
        this.merchantRequestID = merchantRequestID;
    }

    public void setCheckoutRequestID(String checkoutRequestID) {
        this.checkoutRequestID = checkoutRequestID;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseDescription(String responseDescription) {
        this.responseDescription = responseDescription;
    }

    public void setCustomerMessage(String customerMessage) {
        this.customerMessage = customerMessage;
    }
}
