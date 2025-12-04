package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnectionManager {

    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream input = DBConnectionManager.class.getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException("ERROR: Cannot find db.properties in classpath");
            }

            Properties prop = new Properties();
            prop.load(input);

            url = prop.getProperty("db.url");
            user = prop.getProperty("db.user");
            password = prop.getProperty("db.password");

            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize DBConnectionManager", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
