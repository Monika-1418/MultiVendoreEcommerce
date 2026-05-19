import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Multi-Vendor E-Commerce Platform
 * Title   : Multi-Vendor E-Commerce Platform
 * Username: root
 * Admin   : admin
 *
 * ProductVendorDAO.java
 * Handles all Product and Vendor database operations (4 Functions)
 *
 * Required MySQL Tables:
 * ─────────────────────────────────────────────────────────────────────
 * CREATE DATABASE multivendor_ecommerce;
 * USE multivendor_ecommerce;
 *
 * CREATE TABLE vendors (
 *     vendor_id   INT AUTO_INCREMENT PRIMARY KEY,
 *     vendor_name VARCHAR(100) NOT NULL,
 *     email       VARCHAR(100) UNIQUE NOT NULL,
 *     phone       VARCHAR(20),
 *     address     TEXT,
 *     created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 * );
 *
 * CREATE TABLE products (
 *     product_id   INT AUTO_INCREMENT PRIMARY KEY,
 *     vendor_id    INT NOT NULL,
 *     product_name VARCHAR(150) NOT NULL,
 *     category     VARCHAR(80),
 *     price        DECIMAL(10,2) NOT NULL,
 *     stock        INT DEFAULT 0,
 *     created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *     FOREIGN KEY (vendor_id) REFERENCES vendors(vendor_id) ON DELETE CASCADE
 * );
 * ─────────────────────────────────────────────────────────────────────
 */
public class ProductVendorDAO {

    // ═══════════════════════════════════════════════════════════════════
    //  VENDOR FUNCTIONS
    // ═══════════════════════════════════════════════════════════════════

    // ─── Function 1: Add Vendor ───────────────────────────────────────
    public int addVendor(String vendorName, String email, String phone, String address) {
        String sql = "INSERT INTO vendors (vendor_name, email, phone, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, vendorName);
            pst.setString(2, email);
            pst.setString(3, phone);
            pst.setString(4, address);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                ResultSet keys = pst.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    System.out.println("[Vendor] Added → ID: " + id + " | Name: " + vendorName);
                    return id;
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("duplicate")) {
                int existingId = getVendorIdByEmail(email);
                if (existingId != -1) {
                    return existingId;
                }
            }
            System.err.println("[Vendor] Add failed: " + e.getMessage());
        }
        return -1;
    }

    public int getVendorIdByEmail(String email) {
        String sql = "SELECT vendor_id FROM vendors WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("vendor_id");
            }
        } catch (SQLException e) {
            System.err.println("[Vendor] Lookup failed: " + e.getMessage());
        }
        return -1;
    }

    // ─── Function 2: Get All Vendors ──────────────────────────────────
    public List<String[]> getAllVendors() {
        List<String[]> vendors = new ArrayList<>();
        String sql = "SELECT vendor_id, vendor_name, email, phone, address, created_at FROM vendors ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n╔══════ All Vendors — Multi-Vendor E-Commerce Platform ══════╗");
            System.out.printf("║ %-4s %-20s %-25s %-12s ║%n", "ID", "Vendor Name", "Email", "Phone");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");

            while (rs.next()) {
                String[] row = {
                    rs.getString("vendor_id"),
                    rs.getString("vendor_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address"),
                    rs.getString("created_at")
                };
                vendors.add(row);
                System.out.printf("║ %-4s %-20s %-25s %-12s ║%n",
                    row[0], row[1], row[2], row[3]);
            }
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("  Total Vendors: " + vendors.size());

        } catch (SQLException e) {
            System.err.println("[Vendor] Fetch failed: " + e.getMessage());
        }
        return vendors;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PRODUCT FUNCTIONS
    // ═══════════════════════════════════════════════════════════════════

    // ─── Function 3: Add Product ──────────────────────────────────────
    public boolean addProduct(int vendorId, String productName, String category, double price, int stock) {
        String sql = "INSERT INTO products (vendor_id, product_name, category, price, stock) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setInt(1, vendorId);
            pst.setString(2, productName);
            pst.setString(3, category);
            pst.setDouble(4, price);
            pst.setInt(5, stock);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                ResultSet keys = pst.getGeneratedKeys();
                if (keys.next()) {
                    System.out.println("[Product] Added → ID: " + keys.getInt(1)
                        + " | Name: " + productName
                        + " | Price: ₹" + price
                        + " | Stock: " + stock);
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[Product] Add failed: " + e.getMessage());
        }
        return false;
    }

    // ─── Function 4: Search Products by Category ──────────────────────
    public List<String[]> searchProductsByCategory(String category) {
        List<String[]> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, p.category, p.price, p.stock, v.vendor_name "
                   + "FROM products p JOIN vendors v ON p.vendor_id = v.vendor_id "
                   + "WHERE p.category LIKE ? ORDER BY p.price ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, "%" + category + "%");
            ResultSet rs = pst.executeQuery();

            System.out.println("\n╔══════ Products: Category = " + category + " ══════╗");
            System.out.printf("║ %-4s %-22s %-12s %-10s %-6s %-15s ║%n",
                "ID", "Product", "Category", "Price", "Stock", "Vendor");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");

            while (rs.next()) {
                String[] row = {
                    rs.getString("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getString("price"),
                    rs.getString("stock"),
                    rs.getString("vendor_name")
                };
                products.add(row);
                System.out.printf("║ %-4s %-22s %-12s ₹%-9s %-6s %-15s ║%n",
                    row[0], row[1], row[2], row[3], row[4], row[5]);
            }
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("  Results Found: " + products.size());

        } catch (SQLException e) {
            System.err.println("[Product] Search failed: " + e.getMessage());
        }
        return products;
    }

    // ─── Main (Demo) ──────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Multi-Vendor E-Commerce Platform               ║");
        System.out.println("║   Title    : Multi-Vendor E-Commerce Platform    ║");
        System.out.println("║   Username : root  |  Admin : admin              ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        ProductVendorDAO dao = new ProductVendorDAO();

        int techZoneId = dao.addVendor("TechZone India", "techzone@email.com", "9876543210", "Bengaluru, Karnataka");
        if (techZoneId == -1) {
            techZoneId = dao.getVendorIdByEmail("techzone@email.com");
        }
        int fashionHubId = dao.addVendor("FashionHub", "fashion@email.com", "8765432109", "Mumbai, Maharashtra");
        if (fashionHubId == -1) {
            fashionHubId = dao.getVendorIdByEmail("fashion@email.com");
        }

        // Demo: List all vendors
        dao.getAllVendors();

        // Demo: Add products only if the vendor exists
        if (techZoneId > 0) {
            dao.addProduct(techZoneId, "Samsung Galaxy S24", "Electronics", 79999.00, 50);
            dao.addProduct(techZoneId, "HP Laptop 15",        "Electronics", 54999.00, 30);
        }
        if (fashionHubId > 0) {
            dao.addProduct(fashionHubId, "Kurti Set",            "Fashion",     1299.00,  200);
        }

        // Demo: Search by category
        dao.searchProductsByCategory("Electronics");

        DBConnection.closeConnection();
    }
}
