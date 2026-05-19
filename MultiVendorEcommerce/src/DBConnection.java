import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Multi-Vendor E-Commerce Platform
 * Title   : Multi-Vendor E-Commerce Platform
 * Username: root
 * Admin   : admin
 * 
 * DBConnection.java - MySQL Database Connection Manager
 */
public class DBConnection {

    // ─── Database Configuration ───────────────────────────────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3308/multivendor_ecommerce";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "admin";
    private static final String DRIVER   = "com.mysql.cj.jdbc.Driver";

    private static Connection connection = null;

    // ─── Function 1: Get Connection ───────────────────────────────────────────
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DRIVER);
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("[DBConnection] Connected to MySQL successfully.");
                System.out.println("[DBConnection] Host     : localhost:3306");
                System.out.println("[DBConnection] Database : multivendor_ecommerce");
                System.out.println("[DBConnection] Username : " + USERNAME);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection failed: " + e.getMessage());
        }
        return connection;
    }

    // ─── Function 2: Close Connection ────────────────────────────────────────
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DBConnection] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DBConnection] Error closing connection: " + e.getMessage());
        }
    }

    // ─── Function 3: Test Connection ─────────────────────────────────────────
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("[DBConnection] Connection test PASSED.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection test FAILED: " + e.getMessage());
        }
        return false;
    }

    // ─── Function 4: Print DB Info ────────────────────────────────────────────
    public static void printConnectionInfo() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   Multi-Vendor E-Commerce Platform           ║");
        System.out.println("║   Database Connection Info                   ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  Title    : Multi-Vendor E-Commerce Platform ║");
        System.out.printf("║  URL      : %-39s ║%n", URL);
        System.out.printf("║  Username : %-27s ║%n", USERNAME);
        System.out.println("║  Admin    : admin                            ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    // ─── Main (Entry Point for Testing) ──────────────────────────────────────
    public static void main(String[] args) {
        printConnectionInfo();
        testConnection();
        closeConnection();
    }
}
