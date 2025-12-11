package dao;

import jakarta.enterprise.context.ApplicationScoped;
import model.Service;
import util.DBConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ServiceDAO {

    // ======================================================
    // 1️⃣ Add a new service (ADMIN)
    // ======================================================
    public boolean addService(Service s) {
        String sql = "INSERT INTO services (name, description, charge, duration_value, duration_unit, active) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, s.getName());
            stmt.setString(2, s.getDescription());
            stmt.setDouble(3, s.getCharge());

            if (s.getDurationValue() != null) {
                stmt.setInt(4, s.getDurationValue());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            if (s.getDurationUnit() != null) {
                stmt.setString(5, s.getDurationUnit());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            stmt.setInt(6, s.isActive() ? 1 : 0);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding service: " + e.getMessage());
        }
        return false;
    }

    // ======================================================
    // 2️⃣ CUSTOMER — List ONLY active services
    // ======================================================
    public List<Service> getAllActiveServices() {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT * FROM services WHERE active = 1 ORDER BY id DESC";

        try (Connection conn = DBConnectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(extractService(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error listing active services: " + e.getMessage());
        }
        return list;
    }

    // ======================================================
    // 3️⃣ ADMIN — List ALL services
    // ======================================================
    public List<Service> getAllServices() {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT * FROM services ORDER BY id DESC";

        try (Connection conn = DBConnectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(extractService(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error listing all services: " + e.getMessage());
        }
        return list;
    }

    // ======================================================
    // 4️⃣ Get ONE service by ID
    // ======================================================
    public Service getServiceById(int id) {
        String sql = "SELECT * FROM services WHERE id = ?";
        Service service = null;

        try (Connection conn = DBConnectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                service = extractService(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching service by ID: " + e.getMessage());
        }

        return service;
    }

    // ======================================================
    // 5️⃣ CUSTOMER — Services NOT subscribed
    // ======================================================
    public List<Service> getServicesNotSubscribed(int customerId) {
        List<Service> list = new ArrayList<>();

        String sql
                = "SELECT * FROM services "
                + "WHERE active = 1 AND id NOT IN ("
                + "   SELECT service_id FROM subscriptions WHERE customer_id = ?"
                + ") ORDER BY id";

        try (Connection conn = DBConnectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(extractService(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching available services: " + e.getMessage());
        }

        return list;
    }

    // ======================================================
    // 6️⃣ AJAX — Toggle active/inactive
    // ======================================================
    public boolean toggleStatus(int id) {
        String sql
                = "UPDATE services SET active = CASE WHEN active = 1 THEN 0 ELSE 1 END WHERE id = ?";

        try (Connection conn = DBConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ToggleStatus ERROR: " + e.getMessage());
        }
        return false;
    }

    // ======================================================
    // 7️⃣ ADMIN — Update service
    // ======================================================
    public boolean updateService(Service s) {
        String sql = "UPDATE services SET name=?, description=?, charge=?, duration_value=?, duration_unit=?, active=? WHERE id=?";

        try (Connection conn = DBConnectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, s.getName());
            stmt.setString(2, s.getDescription());
            stmt.setDouble(3, s.getCharge());

            if (s.getDurationValue() != null) {
                stmt.setInt(4, s.getDurationValue());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            if (s.getDurationUnit() != null) {
                stmt.setString(5, s.getDurationUnit());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            stmt.setInt(6, s.isActive() ? 1 : 0);
            stmt.setInt(7, s.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating service: " + e.getMessage());
        }
        return false;
    }

    // ======================================================
    // 8️⃣ ADMIN — Hard delete
    // ======================================================
    public boolean deleteService(int id) {
        String sql = "DELETE FROM services WHERE id = ?";

        try (Connection conn = DBConnectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting service: " + e.getMessage());
        }
        return false;
    }

    public boolean serviceHasBillingRecords(int serviceId) {
        String sql = "SELECT COUNT(*) FROM billing WHERE service_id = ?";

        try (Connection con = DBConnectionManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, serviceId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ======================================================
    // Helper: Extract service from ResultSet
    // ======================================================
    private Service extractService(ResultSet rs) throws SQLException {
        Service s = new Service();

        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setDescription(rs.getString("description"));
        s.setCharge(rs.getDouble("charge"));

        int durationVal = rs.getInt("duration_value");
        s.setDurationValue(rs.wasNull() ? null : durationVal);

        s.setDurationUnit(rs.getString("duration_unit"));
        s.setActive(rs.getInt("active") == 1);

        return s;
    }
}
