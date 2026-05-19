# Multi-Vendor E-Commerce Platform
### JDBC + MySQL Java Project

| Field    | Value                              |
|----------|------------------------------------|
| Title    | Multi-Vendor E-Commerce Platform   |
| Username | root                               |
| Admin    | admin                              |
| Language | Java (JDBC)                        |
| Database | MySQL                              |

---

## 📁 Project Structure
```
MultiVendorEcommerce/
├── schema.sql                   ← Run this first in MySQL
├── README.md
└── src/
    ├── DBConnection.java        ← File 1: DB Connection (4 functions)
    ├── ProductVendorDAO.java    ← File 2: Product & Vendor CRUD (4 functions)
    └── OrderCustomerDAO.java    ← File 3: Order & Customer CRUD (8 functions)
```

---

## ⚙️ Setup Instructions

### Step 1 — MySQL Setup
```sql
mysql -u root -padmin < schema.sql
```
Or open MySQL Workbench and run `schema.sql`.

### Step 2 — Add MySQL Connector JAR
Download: https://dev.mysql.com/downloads/connector/j/
Add `mysql-connector-j-x.x.x.jar` to your classpath.

### Step 3 — Compile
```bash
javac -cp .;mysql-connector-j.jar src/*.java
```

### Step 4 — Run
```bash
java -cp .;mysql-connector-j.jar DBConnection
java -cp .;mysql-connector-j.jar ProductVendorDAO
java -cp .;mysql-connector-j.jar OrderCustomerDAO
```

---

## 🔌 DB Connection Config (DBConnection.java)
```java
URL      = "jdbc:mysql://localhost:3306/multivendor_ecommerce"
USERNAME = "root"
PASSWORD = "admin"
```

---

## 📋 10 Functions Summary

### DBConnection.java
| # | Function              | Description                    |
|---|-----------------------|--------------------------------|
| 1 | `getConnection()`     | Get MySQL connection            |
| 2 | `closeConnection()`   | Close MySQL connection          |
| 3 | `testConnection()`    | Ping/test DB connection         |
| 4 | `printConnectionInfo()` | Print DB config details       |

### ProductVendorDAO.java
| # | Function                          | Description              |
|---|-----------------------------------|--------------------------|
| 5 | `addVendor()`                     | Register a new vendor    |
| 6 | `getAllVendors()`                  | List all vendors         |
| 7 | `addProduct()`                    | Add product to vendor    |
| 8 | `searchProductsByCategory()`      | Search products          |

### OrderCustomerDAO.java
| # | Function                   | Description                    |
|---|----------------------------|--------------------------------|
| 9 | `registerCustomer()`       | Register a new customer        |
|10 | `getCustomerById()`        | Fetch customer details         |
|11 | `updateCustomerEmail()`    | Update customer email          |
|12 | `deleteCustomer()`         | Delete a customer              |
|13 | `placeOrder()`             | Place order (with transaction) |
|14 | `getOrdersByCustomer()`    | View all orders of a customer  |
|15 | `updateOrderStatus()`      | Change order status            |
|16 | `getAdminSalesSummary()`   | Admin: revenue by vendor       |

---

## 🗄️ Database Tables
- `vendors` — Vendor details
- `products` — Products linked to vendors
- `customers` — Customer accounts
- `orders` — Orders (with status tracking)

---

*Generated for: Multi-Vendor E-Commerce Platform | Username: root | Admin: admin*
