package beans;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.io.IOException;

@Named("logoutBean")
@SessionScoped
public class LogoutBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public String logout() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(FacesContext.getCurrentInstance()
                            .getExternalContext()
                            .getRequestContextPath() + "/login.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
