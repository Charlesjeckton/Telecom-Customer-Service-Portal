package beans.customer;

import beans.LoginBean;
import dao.SubscriptionDAO;
import model.Subscription;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named("subscriptionBean")
@ViewScoped
public class CustomerSubscriptionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Subscription> subscriptions;

    private final SubscriptionDAO dao = new SubscriptionDAO();

    // ============================
    // Load subscriptions of logged-in user
    // ============================
    @PostConstruct
    public void init() {

        // Retrieve loginBean stored in session map
        LoginBean loginBean = (LoginBean) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("loginBean");

        if (loginBean == null) {
            System.out.println("❌ loginBean is NULL — user not logged in.");
            return;
        }

        int customerId = loginBean.getCustomerId();
        System.out.println("🔍 Loading subscriptions for customerId = " + customerId);

        subscriptions = dao.getSubscriptionsByCustomerId(customerId);

        if (subscriptions == null || subscriptions.isEmpty()) {
            System.out.println("⚠ No subscriptions found for this user.");
        }
    }

    // ============================
    // Getters
    // ============================
    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }
}
