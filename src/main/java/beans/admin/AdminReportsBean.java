package beans.admin;

import dao.BillingDAO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named("adminReportsBean")
@RequestScoped
public class AdminReportsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private BillingDAO billingDAO;

    private int paidCount;
    private int unpaidCount;
    private int totalBills;

    private String monthLabels; // JS array string e.g. ['Jan','Feb']
    private String monthValues; // JS array string e.g. [1200,1500]

    @PostConstruct
    public void init() {
    // Load counts
    paidCount = billingDAO.getPaidBills().size();
    unpaidCount = billingDAO.getUnpaidBills().size();
    totalBills = paidCount + unpaidCount;

    // Load monthly totals (Double values)
    Map<String, Double> monthlyTotals = billingDAO.getMonthlyTotals();

    // Convert labels to JS array format: ["Jan","Feb","Mar"]
    monthLabels = monthlyTotals.keySet()
            .stream()
            .map(label -> "\"" + label + "\"")
            .collect(Collectors.joining(",", "[", "]"));

    // Convert totals to JS array format: [1200.50, 800.00]
    monthValues = monthlyTotals.values()
            .stream()
            .map(String::valueOf)
            .collect(Collectors.joining(",", "[", "]"));
}


    // ================== GETTERS ==================

    public int getPaidCount() {
        return paidCount;
    }

    public int getUnpaidCount() {
        return unpaidCount;
    }

    public int getTotalBills() {
        return totalBills;
    }

    public String getMonthLabels() {
        return monthLabels;
    }

    public String getMonthValues() {
        return monthValues;
    }
}
