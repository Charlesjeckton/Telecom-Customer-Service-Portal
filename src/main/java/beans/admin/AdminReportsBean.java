package beans.admin;

import dao.BillingDAO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Named("adminReportsBean")
@RequestScoped
public class AdminReportsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private BillingDAO billingDAO;

    private int paidCount;
    private int unpaidCount;
    private int totalBills;

    private String monthLabels; // JS array string e.g. ["Jan","Feb"]
    private String monthValues; // JS array string e.g. [1200,1500]

    @PostConstruct
    public void init() {

        // ============ BASIC COUNTS ============
        paidCount = billingDAO.getPaidBills().size();
        unpaidCount = billingDAO.getUnpaidBills().size();
        totalBills = paidCount + unpaidCount;

        // ============ MONTHLY TOTALS ============
        Map<String, Double> monthlyTotals = billingDAO.getMonthlyTotals();

        if (monthlyTotals == null || monthlyTotals.isEmpty()) {
            monthLabels = "[]";
            monthValues = "[]";
            return;
        }

        // Ensure month sorting (Jan → Dec) if keys are month names or numbers
        Map<String, Double> sortedMonthlyTotals = monthlyTotals.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        // Convert labels to JS array format: ["Jan","Feb","Mar"]
        monthLabels = sortedMonthlyTotals.keySet()
                .stream()
                .map(label -> "\"" + label + "\"")
                .collect(Collectors.joining(",", "[", "]"));

        // Convert totals to JS array format: [1200.50,800.00]
        monthValues = sortedMonthlyTotals.values()
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
