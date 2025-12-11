package beans.admin;

import dao.SubscriptionDAO;
import model.Subscription;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import java.io.Serializable;
import java.util.List;

@Named("adminSubscriptionBean")
@RequestScoped
public class AdminSubscriptionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SubscriptionDAO subscriptionDAO;

    private List<Subscription> subscriptions;

    // Load subscriptions for display
    public List<Subscription> getSubscriptions() {
        if (subscriptions == null) {
            subscriptions = subscriptionDAO.getAllSubscriptions();
        }
        return subscriptions;
    }
}
