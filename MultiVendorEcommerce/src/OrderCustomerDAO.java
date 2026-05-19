import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Multi-Vendor E-Commerce Platform
 * Title   : Multi-Vendor E-Commerce Platform
 * Username: root
 * Admin   : admin
 *
 * OrderCustomerDAO.java
 * Handles Order & Customer CRUD operations (6 Functions)
 *
 * Required MySQL Tables:
 * ─────────────────────────────────────────────────────────────────────
 * CREATE TABLE customers (
 *     customer_id   INT AUTO_INCREMENT PRIMARY KEY,
 *     full_name     VARCHAR(100) NOT NULL,
 *     email         VARCHAR(100) UNIQUE NOT NULL,
 *     phone         VARCHAR(20),
 *     address       TEXT,
 *     created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 * );
 *
 * CREATE TABLE orders (
 *     order_id      INT AUTO_INCREMENT PRIMARY KEY,
 *     customer_id   INT NOT NULL,
 *     product_id    INT NOT NULL,
 *     quantity      INT NOT NULL DEFAULT 1,
 *     total_price   DECIMAL(10,2) NOT NULL,
 *     status        ENUM('Pending','Confirmed','Shipped','Delivered','Cancelled') DEFAULT 'Pending',
 *     order_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *     FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
 *     FOREIGN KEY (product_id)  REFERENCES products(product_id)  ON DELETE CASCADE
 * );
 * ─────────────────────────────────────────────────────────────────────
 */
public class OrderCustomerDAO {

    // ═══════════════════════════════════════════════════════════════════
    //  CUSTOMER FUNCTIONS
    // ═══════════════════════════════════════════════════════════════════

    // ─── Function 1: Register Customer ───────────────────────────────
    public int registerCustomer(String fullName, String email, String phone, String address) {
        String sql = "INSERT INTO customers (full_name, email, phone, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, fullName);
            pst.setString(2, email);
            pst.setString(3, phone);
            pst.setString(4, address);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                ResultSet keys = pst.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    System.out.println("[Customer] Registered → ID: " + id + " | Name: " + fullName);
                    return id;
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("duplicate")) {
                int existingId = getCustomerIdByEmail(email);
                if (existingId != -1) {
                    return existingId;
                }
            }
            System.err.println("[Customer] Register failed: " + e.getMessage());
        }
        return -1;
    }

    public int getCustomerIdByEmail(String email) {
        String sql = "SELECT customer_id FROM customers WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("customer_id");
            }
        } catch (SQLException e) {
            System.err.println("[Customer] Lookup failed: " + e.getMessage());
        }
        return -1;
    }

    // ─── Function 2: Get Customer by ID ──────────────────────────────
    public void getCustomerById(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println("\n╔══════ Customer Details ══════╗");
                System.out.println("║ ID      : " + rs.getInt("customer_id"));
                System.out.println("║ Name    : " + rs.getString("full_name"));
                System.out.println("║ Email   : " + rs.getString("email"));
                System.out.println("║ Phone   : " + rs.getString("phone"));
                System.out.println("║ Address : " + rs.getString("address"));
                System.out.println("║ Joined  : " + rs.getTimestamp("created_at"));
                System.out.println("╚══════════════════════════════╝");
            } else {
                System.out.println("[Customer] No customer found with ID: " + customerId);
            }
        } catch (SQLException e) {
            System.err.println("[Customer] Fetch failed: " + e.getMessage());
        }
    }

    // ─── Function 3: Update Customer Email ───────────────────────────
    public boolean updateCustomerEmail(int customerId, String newEmail) {
        String sql = "UPDATE customers SET email = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, newEmail);
            pst.setInt(2, customerId);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("[Customer] Email updated → Customer ID: " + customerId
                    + " | New Email: " + newEmail);
                return true;
            } else {
                System.out.println("[Customer] No customer found with ID: " + customerId);
            }
        } catch (SQLException e) {
            System.err.println("[Customer] Update failed: " + e.getMessage());
        }
        return false;
    }

    // ─── Function 4: Delete Customer ─────────────────────────────────
    public boolean deleteCustomer(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, customerId);
            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("[Customer] Deleted → Customer ID: " + customerId);
                return true;
            } else {
                System.out.println("[Customer] No customer found with ID: " + customerId);
            }
        } catch (SQLException e) {
            System.err.println("[Customer] Delete failed: " + e.getMessage());
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ORDER FUNCTIONS
    // ═══════════════════════════════════════════════════════════════════

    // ─── Function 5: Place Order ──────────────────────────────────────
    public int placeOrder(int customerId, int productId, int quantity) {
        // Get product price
        String priceSQL = "SELECT price, stock FROM products WHERE product_id = ?";
        String orderSQL = "INSERT INTO orders (customer_id, product_id, quantity, total_price) VALUES (?, ?, ?, ?)";
        String stockSQL = "UPDATE products SET stock = stock - ? WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Get price and check stock
            PreparedStatement pricePst = conn.prepareStatement(priceSQL);
            pricePst.setInt(1, productId);
            ResultSet rs = pricePst.executeQuery();

            if (!rs.next()) {
                System.err.println("[Order] Product not found: " + productId);
                conn.rollback();
                return -1;
            }

            double price = rs.getDouble("price");
            int stock    = rs.getInt("stock");

            if (stock < quantity) {
                System.err.println("[Order] Insufficient stock. Available: " + stock + " | Requested: " + quantity);
                conn.rollback();
                return -1;
            }

            double totalPrice = price * quantity;

            // Step 2: Insert order
            PreparedStatement orderPst = conn.prepareStatement(orderSQL, Statement.RETURN_GENERATED_KEYS);
            orderPst.setInt(1, customerId);
            orderPst.setInt(2, productId);
            orderPst.setInt(3, quantity);
            orderPst.setDouble(4, totalPrice);
            orderPst.executeUpdate();

            ResultSet keys = orderPst.getGeneratedKeys();
            int orderId = -1;
            if (keys.next()) orderId = keys.getInt(1);

            // Step 3: Reduce stock
            PreparedStatement stockPst = conn.prepareStatement(stockSQL);
            stockPst.setInt(1, quantity);
            stockPst.setInt(2, productId);
            stockPst.executeUpdate();

            conn.commit(); // Commit transaction
            System.out.println("[Order] Placed Successfully!");
            System.out.println("        Order ID   : " + orderId);
            System.out.println("        Customer ID: " + customerId);
            System.out.println("        Product ID : " + productId);
            System.out.println("        Quantity   : " + quantity);
            System.out.printf ("        Total      : ₹%.2f%n", totalPrice);
            return orderId;

        } catch (SQLException e) {
            System.err.println("[Order] Place failed: " + e.getMessage());
        }
        return -1;
    }

    // ─── Function 6: Get All Orders for Customer ──────────────────────
    public List<String[]> getOrdersByCustomer(int customerId) {
        List<String[]> orders = new ArrayList<>();
        String sql = "SELECT o.order_id, p.product_name, v.vendor_name, o.quantity, "
                   + "o.total_price, o.status, o.order_date "
                   + "FROM orders o "
                   + "JOIN products p ON o.product_id = p.product_id "
                   + "JOIN vendors v  ON p.vendor_id = v.vendor_id "
                   + "WHERE o.customer_id = ? ORDER BY o.order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();

            System.out.println("\n╔══════ Orders for Customer ID: " + customerId + " ══════╗");
            System.out.printf("║ %-6s %-22s %-15s %-4s %-10s %-11s ║%n",
                "OrdID", "Product", "Vendor", "Qty", "Total", "Status");
            System.out.println("╠══════════════════════════════════════════════════════════╣");

            while (rs.next()) {
                String[] row = {
                    rs.getString("order_id"),
                    rs.getString("product_name"),
                    rs.getString("vendor_name"),
                    rs.getString("quantity"),
                    rs.getString("total_price"),
                    rs.getString("status"),
                    rs.getString("order_date")
                };
                orders.add(row);
                System.out.printf("║ %-6s %-22s %-15s %-4s ₹%-9s %-11s ║%n",
                    row[0], row[1], row[2], row[3], row[4], row[5]);
            }
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            System.out.println("  Total Orders: " + orders.size());

        } catch (SQLException e) {
            System.err.println("[Order] Fetch failed: " + e.getMessage());
        }
        return orders;
    }

    // ─── Function 7: Update Order Status (Bonus) ──────────────────────
    public boolean updateOrderStatus(int orderId, String status) {
        String[] valid = {"Pending", "Confirmed", "Shipped", "Delivered", "Cancelled"};
        boolean isValid = false;
        for (String s : valid) { if (s.equalsIgnoreCase(status)) { isValid = true; break; } }
        if (!isValid) {
            System.err.println("[Order] Invalid status: " + status);
            return false;
        }

        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, status);
            pst.setInt(2, orderId);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("[Order] Status Updated → Order ID: " + orderId + " | Status: " + status);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[Order] Status update failed: " + e.getMessage());
        }
        return false;
    }

    // ─── Function 8: Get Sales Summary (Admin Report) ────────────────
    public void getAdminSalesSummary() {
        String sql = "SELECT v.vendor_name, COUNT(o.order_id) AS total_orders, "
                   + "SUM(o.total_price) AS total_revenue "
                   + "FROM orders o "
                   + "JOIN products p ON o.product_id = p.product_id "
                   + "JOIN vendors v ON p.vendor_id = v.vendor_id "
                   + "GROUP BY v.vendor_id, v.vendor_name ORDER BY total_revenue DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n╔══════ ADMIN SALES SUMMARY — Multi-Vendor Platform ══════╗");
            System.out.println("║  Title    : Multi-Vendor E-Commerce Platform            ║");
            System.out.println("║  Username : root     |  Admin : admin                  ║");
            System.out.println("╠═══════════════════════════════════════════════════════════╣");
            System.out.printf("║ %-22s %-14s %-16s ║%n", "Vendor", "Total Orders", "Revenue (₹)");
            System.out.println("╠═══════════════════════════════════════════════════════════╣");

            double grandTotal = 0;
            int    totalOrders = 0;
            while (rs.next()) {
                String vendor  = rs.getString("vendor_name");
                int    orders  = rs.getInt("total_orders");
                double revenue = rs.getDouble("total_revenue");
                grandTotal  += revenue;
                totalOrders += orders;
                System.out.printf("║ %-22s %-14d ₹%-15.2f ║%n", vendor, orders, revenue);
            }
            System.out.println("╠═══════════════════════════════════════════════════════════╣");
            System.out.printf("║ %-22s %-14d ₹%-15.2f ║%n", "GRAND TOTAL", totalOrders, grandTotal);
            System.out.println("╚═══════════════════════════════════════════════════════════╝");

        } catch (SQLException e) {
            System.err.println("[Admin] Sales summary failed: " + e.getMessage());
        }
    }

    // ─── Main (Demo) ──────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Multi-Vendor E-Commerce Platform               ║");
        System.out.println("║   Title    : Multi-Vendor E-Commerce Platform    ║");
        System.out.println("║   Username : root  |  Admin : admin              ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        OrderCustomerDAO dao = new OrderCustomerDAO();

        // Register customers
        int cust1 = dao.registerCustomer("Rahul Sharma", "rahul@email.com", "9988776655", "Delhi");
        if (cust1 == -1) {
            cust1 = dao.getCustomerIdByEmail("rahul@email.com");
        }
        int cust2 = dao.registerCustomer("Priya Nair",   "priya@email.com", "8877665544", "Bengaluru");
        if (cust2 == -1) {
            cust2 = dao.getCustomerIdByEmail("priya@email.com");
        }

        if (cust1 < 1 || cust2 < 1) {
            System.err.println("[OrderCustomerDAO] Cannot continue without valid customer IDs.");
            DBConnection.closeConnection();
            return;
        }

        // Get customer info
        dao.getCustomerById(cust1);

        // Update email
        dao.updateCustomerEmail(cust1, "rahul.sharma@gmail.com");

        // Place orders (assumes products from ProductVendorDAO exist)
        int ord1 = dao.placeOrder(cust1, 1, 2);
        int ord2 = dao.placeOrder(cust2, 2, 1);

        // Update order status
        dao.updateOrderStatus(ord1, "Confirmed");
        dao.updateOrderStatus(ord2, "Shipped");

        // Get orders by customer
        dao.getOrdersByCustomer(cust1);

        // Admin summary
        dao.getAdminSalesSummary();

        // Delete customer (demo)
        // dao.deleteCustomer(cust2);

        DBConnection.closeConnection();
    }
}
