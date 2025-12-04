package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestDB {
    public static void main(String[] args) {
        try (Connection conn = DBConnectionManager.getConnection()) {
            System.out.println("✅ Database connected successfully!");

            // Optional: run a simple query
            PreparedStatement ps = conn.prepareStatement("SELECT NOW() AS current_time");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Database time: " + rs.getString("current_time"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Failed to connect to the database.");
        }
    }
}
