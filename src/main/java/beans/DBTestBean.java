package beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import util.DBConnectionManager;
import java.sql.Connection;

@Named
@RequestScoped
public class DBTestBean {

    private String message;

    public String getMessage() {
        return message;
    }

    public void testConnection() {
        try (Connection conn = DBConnectionManager.getConnection()) {
            message = "✅ Database connected successfully!";
        } catch (Exception e) {
            message = "❌ Failed to connect: " + e.getMessage();
            e.printStackTrace();
        }
    }
}
