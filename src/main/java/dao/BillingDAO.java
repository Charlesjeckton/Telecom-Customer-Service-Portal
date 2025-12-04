package dao;

import model.Billing;
import util.DBConnectionManager;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.*;
import java.util.*;

@ApplicationScoped
public class BillingDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Generate a Bill
    public boolean generateBill(Billing bill) {
        String sql = "INSERT INTO billing (customer_id, service_id, amount, billing_date, paid) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bill.getCustomerId());
            stmt.setInt(2, bill.getServiceId());
            stmt.setDouble(3, bill.getAmount());

            if (bill.getBillingDate() != null) {
                stmt.setTimestamp(4, new Timestamp(bill.getBillingDate().getTime()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }

            stmt.setBoolean(5, bill.isPaid());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error generating bill: " + e.getMessage());
            return false;
        }
    }

    // Extract Billing Object
    private Billing extractBilling(ResultSet rs) throws SQLException {
        Billing bill = new Billing();

        bill.setId(rs.getInt("id"));
        bill.setCustomerId(rs.getInt("customer_id"));
        bill.setServiceId(rs.getInt("service_id"));
        bill.setAmount(rs.getDouble("amount"));

        Timestamp ts = rs.getTimestamp("billing_date");
        if (ts != null) bill.setBillingDate(new java.util.Date(ts.getTime()));

        bill.setPaid(rs.getBoolean("paid"));

        try { bill.setServiceName(rs.getString("service_name")); } catch (Exception ignored) {}
        try { bill.setCustomerName(rs.getString("customer_name")); } catch (Exception ignored) {}
        try { bill.setCustomerEmail(rs.getString("customer_email")); } catch (Exception ignored) {}

        return bill;
    }

    // Get All Bills
    public List<Billing> getAllBills() {
        List<Billing> list = new ArrayList<>();

        String sql = "SELECT b.*, c.name AS customer_name, c.email AS customer_email, "
                   + "s.name AS service_name "
                   + "FROM billing b "
                   + "LEFT JOIN customers c ON b.customer_id = c.id "
                   + "LEFT JOIN services s ON b.service_id = s.id "
                   + "ORDER BY b.id DESC";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) list.add(extractBilling(rs));

        } catch (SQLException e) {
            System.err.println("Error fetching all bills: " + e.getMessage());
        }
        return list;
    }

    // Get Paid Bills
    public List<Billing> getPaidBills() {
        List<Billing> list = new ArrayList<>();

        String sql = "SELECT b.*, c.name AS customer_name, c.email AS customer_email, "
                   + "s.name AS service_name "
                   + "FROM billing b "
                   + "LEFT JOIN customers c ON b.customer_id = c.id "
                   + "LEFT JOIN services s ON b.service_id = s.id "
                   + "WHERE b.paid = 1 "
                   + "ORDER BY b.billing_date DESC";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) list.add(extractBilling(rs));

        } catch (SQLException e) {
            System.err.println("Error fetching paid bills: " + e.getMessage());
        }
        return list;
    }

    // Get Unpaid Bills
    public List<Billing> getUnpaidBills() {
        List<Billing> list = new ArrayList<>();

        String sql = "SELECT b.*, c.name AS customer_name, c.email AS customer_email, "
                   + "s.name AS service_name "
                   + "FROM billing b "
                   + "LEFT JOIN customers c ON b.customer_id = c.id "
                   + "LEFT JOIN services s ON b.service_id = s.id "
                   + "WHERE b.paid = 0 "
                   + "ORDER BY b.billing_date DESC";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) list.add(extractBilling(rs));

        } catch (SQLException e) {
            System.err.println("Error fetching unpaid bills: " + e.getMessage());
        }
        return list;
    }

    // Mark Bill Paid / Unpaid
    public boolean markBillAsPaid(int billId) {
        String sql = "UPDATE billing SET paid = 1 WHERE id = ?";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, billId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error marking bill paid: " + e.getMessage());
            return false;
        }
    }

    public boolean markBillAsUnpaid(int billId) {
        String sql = "UPDATE billing SET paid = 0 WHERE id = ?";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, billId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error marking bill unpaid: " + e.getMessage());
            return false;
        }
    }

    // Count Paid / Unpaid
    public int countPaidBills() {
        String sql = "SELECT COUNT(*) FROM billing WHERE paid = 1";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) { e.printStackTrace(); }

        return 0;
    }

    public int countUnpaidBills() {
        String sql = "SELECT COUNT(*) FROM billing WHERE paid = 0";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) { e.printStackTrace(); }

        return 0;
    }

    // Count Paid/Unpaid by Customer
    public int countPaidBillsByCustomer(int customerId) {
        String sql = "SELECT COUNT(*) FROM billing WHERE customer_id = ? AND paid = 1";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) { e.printStackTrace(); }

        return 0;
    }

    public int countUnpaidBillsByCustomer(int customerId) {
        String sql = "SELECT COUNT(*) FROM billing WHERE customer_id = ? AND paid = 0";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) { e.printStackTrace(); }

        return 0;
    }
    // ===============================
// Monthly Totals (Customer-Specific)
// ===============================
public Map<String, Double> getMonthlyTotalsByCustomer(int customerId) {
    Map<String, Double> totals = new LinkedHashMap<>();

    String sql = "SELECT DATE_FORMAT(billing_date, '%Y-%m') AS month, SUM(amount) AS total "
               + "FROM billing "
               + "WHERE customer_id = ? "
               + "GROUP BY DATE_FORMAT(billing_date, '%Y-%m') "
               + "ORDER BY month ASC";

    try (Connection conn = DBConnectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, customerId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            totals.put(rs.getString("month"), rs.getDouble("total"));
        }

    } catch (SQLException e) {
        System.err.println("Error fetching monthly totals: " + e.getMessage());
    }

    return totals;
}

    
    public Map<String, Double> getMonthlyTotals() {
    Map<String, Double> totals = new LinkedHashMap<>();

    String sql = "SELECT DATE_FORMAT(billing_date, '%Y-%m') AS month, SUM(amount) AS total "
               + "FROM billing "
               + "GROUP BY DATE_FORMAT(billing_date, '%Y-%m') "
               + "ORDER BY month ASC";

    try (Connection conn = DBConnectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            totals.put(rs.getString("month"), rs.getDouble("total"));
        }

    } catch (SQLException e) {
        System.err.println("Error fetching monthly totals: " + e.getMessage());
    }

    return totals;
}

// Get PAID bills for a specific customer
public List<Billing> getPaidBillsByCustomer(int customerId) {
    List<Billing> list = new ArrayList<>();

    String sql = "SELECT b.*, s.name AS service_name "
               + "FROM billing b "
               + "LEFT JOIN services s ON b.service_id = s.id "
               + "WHERE b.customer_id = ? AND b.paid = 1 "
               + "ORDER BY b.billing_date DESC";

    try (Connection conn = DBConnectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, customerId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            list.add(extractBilling(rs));
        }

    } catch (SQLException e) {
        System.err.println("Error fetching PAID bills by customer: " + e.getMessage());
    }

    return list;
}


// Get UNPAID bills for a specific customer
public List<Billing> getUnpaidBillsByCustomer(int customerId) {
    List<Billing> list = new ArrayList<>();

    String sql = "SELECT b.*, s.name AS service_name "
               + "FROM billing b "
               + "LEFT JOIN services s ON b.service_id = s.id "
               + "WHERE b.customer_id = ? AND b.paid = 0 "
               + "ORDER BY b.billing_date DESC";

    try (Connection conn = DBConnectionManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, customerId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) list.add(extractBilling(rs));

    } catch (SQLException e) {
        System.err.println("Error fetching customer unpaid bills: " + e.getMessage());
    }

    return list;
}

    // Get Bills for 1 Customer
    public List<Billing> getBillsByCustomer(int customerId) {
        List<Billing> list = new ArrayList<>();

        String sql = "SELECT b.*, s.name AS service_name "
                   + "FROM billing b "
                   + "LEFT JOIN services s ON b.service_id = s.id "
                   + "WHERE b.customer_id = ? "
                   + "ORDER BY b.id DESC";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(extractBilling(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching bills by customer: " + e.getMessage());
        }

        return list;
    }
}
