package beans.customer;

import dao.BillingDAO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import beans.LoginBean;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

@Named("customerReportsBean")
@RequestScoped
public class CustomerReportsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private BillingDAO billingDAO;

    private int paidCount;
    private int unpaidCount;
    private int totalBills;

    private String monthLabels;
    private String monthValues;

    @PostConstruct
    public void init() {

        Integer customerId = getLoggedCustomerId();
        if (customerId == null) {
            paidCount = 0;
            unpaidCount = 0;
            totalBills = 0;
            monthLabels = "[]";
            monthValues = "[]";
            return;
        }

        // Counts for customer
        paidCount = billingDAO.countPaidBillsByCustomer(customerId);
        unpaidCount = billingDAO.countUnpaidBillsByCustomer(customerId);
        totalBills = paidCount + unpaidCount;

        // ===== Monthly totals for THIS customer only (Double) =====
        Map<String, Double> monthlyTotals = billingDAO.getMonthlyTotalsByCustomer(customerId);

        // Convert keys to JS array: ["2025-01","2025-02"]
        monthLabels = monthlyTotals.keySet()
                .stream()
                .map(key -> "\"" + key + "\"")
                .collect(Collectors.joining(",", "[", "]"));

        // Convert values to JS array: [1200.50, 850.00]
        monthValues = monthlyTotals.values()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private Integer getLoggedCustomerId() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) return null;

        LoginBean loginBean = (LoginBean) ctx.getExternalContext()
                .getSessionMap()
                .get("loginBean");

        if (loginBean == null || loginBean.getCustomerId() == 0) {
            return null;
        }

        return loginBean.getCustomerId();
    }

    // Getters
    public int getPaidCount() { return paidCount; }
    public int getUnpaidCount() { return unpaidCount; }
    public int getTotalBills() { return totalBills; }
    public String getMonthLabels() { return monthLabels; }
    public String getMonthValues() { return monthValues; }
}
