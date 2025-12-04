package beans.customer;

import beans.LoginBean;
import dao.SubscriptionDAO;
import dao.ServiceDAO;
import dao.BillingDAO;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import model.Billing;
import model.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Named("addSubscriptionBean")
@ViewScoped
public class AddSubscriptionBean implements Serializable {

    private Integer serviceId;

    private final SubscriptionDAO subDAO = new SubscriptionDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();
    private final BillingDAO billingDAO = new BillingDAO();

    // ------------------------------------
    // Getter / Setter
    // ------------------------------------
    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    // ------------------------------------
    // Add Subscription
    // ------------------------------------
    public void addSubscription() {

        FacesContext ctx = FacesContext.getCurrentInstance();

        // 1️⃣ Validate login
        LoginBean login = (LoginBean) ctx.getExternalContext()
                .getSessionMap()
                .get("loginBean");

        if (login == null || login.getCustomerId() == 0) {
            addFlash("error", "You must be logged in.");
            redirect("services.xhtml");
            return;
        }

        int customerId = login.getCustomerId();

        // 2️⃣ Validate service id
        if (serviceId == null) {
            addFlash("error", "Invalid service ID.");
            redirect("services.xhtml");
            return;
        }

        // 3️⃣ Load service
        Service service = serviceDAO.getServiceById(serviceId);
        if (service == null) {
            addFlash("error", "Service not found.");
            redirect("services.xhtml");
            return;
        }

        // 4️⃣ Calculate dates
        LocalDateTime purchaseDate = LocalDateTime.now();
        LocalDateTime expiryDate = calculateExpiry(purchaseDate, service);

        java.util.Date purchase = java.util.Date.from(
                purchaseDate.atZone(ZoneId.systemDefault()).toInstant()
        );

        java.util.Date expiry = java.util.Date.from(
                expiryDate.atZone(ZoneId.systemDefault()).toInstant()
        );

        // 5️⃣ Add subscription into DB
        boolean added = subDAO.addSubscription(customerId, serviceId, purchase, expiry);

        if (!added) {
            addFlash("error", "Failed to add subscription.");
            redirect("services.xhtml");
            return;
        }

        // 6️⃣ Create billing record
        Billing bill = new Billing();
        bill.setCustomerId(customerId);
        bill.setServiceId(serviceId);
        bill.setAmount(service.getCharge());
        bill.setBillingDate(purchase);
        bill.setPaid(false);

        billingDAO.generateBill(bill);

        // 7️⃣ Redirect with Flash success
        addFlash("success", "Subscription added successfully!");
        redirect("subscriptions.xhtml");
    }

    // ------------------------------------
    // Expiry Calculator
    // ------------------------------------
    private LocalDateTime calculateExpiry(LocalDateTime start, Service service) {
        int duration = service.getDurationValue();
        String unit = service.getDurationUnit().toUpperCase();

        switch (unit) {
            case "HOUR":
                return start.plus(duration, ChronoUnit.HOURS);
            case "DAY":
                return start.plus(duration, ChronoUnit.DAYS);
            case "WEEK":
                return start.plus(duration, ChronoUnit.WEEKS);
            case "MONTH":
                return start.plus(duration, ChronoUnit.MONTHS);
            default:
                return start.plusDays(1);
        }
    }

    // ------------------------------------
    // Flash Message (success / error)
    // ------------------------------------
    private void addFlash(String type, String msg) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getExternalContext().getFlash().put("flash_message", msg);
        ctx.getExternalContext().getFlash().put("flash_type", type);
    }

    // ------------------------------------
    // Redirect Helper
    // ------------------------------------
    private void redirect(String page) {
        try {
            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .redirect(page);
        } catch (Exception ignored) {
        }
    }
}
