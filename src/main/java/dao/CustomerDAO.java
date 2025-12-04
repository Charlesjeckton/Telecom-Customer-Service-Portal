package dao;

import jakarta.enterprise.context.ApplicationScoped;
import model.Customer;
import model.User;
import util.DBConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class CustomerDAO {

    /**
     * Register a new customer with a linked user account.
     * Handles:
     *  - duplicate username
     *  - duplicate email
     *  - full transaction (rollback on failure)
     */
    public String registerCustomer(Customer c, User u) {

        String checkUserSql = "SELECT id FROM users WHERE username = ?";
        String checkEmailSql = "SELECT id FROM customers WHERE email = ?";
        String userSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        String customerSql = "INSERT INTO customers (name, phone, email, registration_date, user_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionManager.getConnection()) {

            conn.setAutoCommit(false); // Begin transaction

            // 1️⃣ Check if username exists
            try (PreparedStatement ps = conn.prepareStatement(checkUserSql)) {
                ps.setString(1, u.getUsername());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return "Username already exists";
                }
            }

            // 2️⃣ Check if email exists
            try (PreparedStatement ps = conn.prepareStatement(checkEmailSql)) {
                ps.setString(1, c.getEmail());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return "Email already exists";
                }
            }

            int userId;

            // 3️⃣ Insert user record
            try (PreparedStatement ps = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, u.getUsername());
                ps.setString(2, u.getPassword());
                ps.setString(3, u.getRole());

                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return "Failed to create user account";
                }

                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    userId = keys.getInt(1);
                } else {
                    conn.rollback();
                    return "Failed to retrieve user ID";
                }
            }

            // 4️⃣ Insert customer
            try (PreparedStatement ps = conn.prepareStatement(customerSql)) {

                ps.setString(1, c.getName());
                ps.setString(2, c.getPhoneNumber());
                ps.setString(3, c.getEmail());
                ps.setDate(4, new java.sql.Date(c.getRegistrationDate().getTime()));
                ps.setInt(5, userId);

                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return "Failed to create customer record";
                }
            }

            conn.commit();
            return "SUCCESS";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        }
    }


    // 🔎 Get customer by ID
    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Customer c = new Customer();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setPhoneNumber(rs.getString("phone")); // FIXED
                c.setEmail(rs.getString("email"));

                Date regDate = rs.getDate("registration_date");
                if (regDate != null)
                    c.setRegistrationDate(new java.util.Date(regDate.getTime()));

                return c;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // Legacy insert method (kept for compatibility)
    public boolean insertCustomer(Customer c) {

        String sql = "INSERT INTO customers (name, phone, email, registration_date, user_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getName());
            stmt.setString(2, c.getPhoneNumber()); // FIXED
            stmt.setString(3, c.getEmail());
            stmt.setDate(4, new java.sql.Date(c.getRegistrationDate().getTime()));
            stmt.setInt(5, c.getUserId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting customer: " + e.getMessage());
            return false;
        }
    }


    // 🧾 Get all customers
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Customer c = new Customer();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setPhoneNumber(rs.getString("phone")); // FIXED
                c.setEmail(rs.getString("email"));

                Date regDate = rs.getDate("registration_date");
                if (regDate != null)
                    c.setRegistrationDate(new java.util.Date(regDate.getTime()));

                list.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
