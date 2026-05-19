-- ═══════════════════════════════════════════════════════════════
--  Multi-Vendor E-Commerce Platform
--  Title    : Multi-Vendor E-Commerce Platform
--  Username : root
--  Admin    : admin
--  File     : schema.sql  — Run this FIRST before the Java code
-- ═══════════════════════════════════════════════════════════════

DROP DATABASE IF EXISTS multivendor_ecommerce;
CREATE DATABASE multivendor_ecommerce;
USE multivendor_ecommerce;

-- ── Vendors ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vendors (
    vendor_id   INT AUTO_INCREMENT PRIMARY KEY,
    vendor_name VARCHAR(100) NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL,
    phone       VARCHAR(20),
    address     TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── Products ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS products (
    product_id   INT AUTO_INCREMENT PRIMARY KEY,
    vendor_id    INT NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    category     VARCHAR(80),
    price        DECIMAL(10,2) NOT NULL,
    stock        INT DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vendor_id) REFERENCES vendors(vendor_id) ON DELETE CASCADE
);

-- ── Customers ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS customers (
    customer_id  INT AUTO_INCREMENT PRIMARY KEY,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(100) UNIQUE NOT NULL,
    phone        VARCHAR(20),
    address      TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── Orders ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
    order_id    INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    product_id  INT NOT NULL,
    quantity    INT NOT NULL DEFAULT 1,
    total_price DECIMAL(10,2) NOT NULL,
    status      ENUM('Pending','Confirmed','Shipped','Delivered','Cancelled') DEFAULT 'Pending',
    order_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id)  REFERENCES products(product_id)  ON DELETE CASCADE
);

-- ── Sample Data ─────────────────────────────────────────────────
INSERT INTO vendors (vendor_name, email, phone, address) VALUES
  ('TechZone India',  'techzone@email.com',  '9876543210', 'Bengaluru, Karnataka'),
  ('FashionHub',      'fashion@email.com',   '8765432109', 'Mumbai, Maharashtra'),
  ('GroceryMart',     'grocery@email.com',   '7654321098', 'Chennai, Tamil Nadu');

INSERT INTO products (vendor_id, product_name, category, price, stock) VALUES
  (1, 'Samsung Galaxy S24',  'Electronics', 79999.00, 50),
  (1, 'HP Laptop 15',        'Electronics', 54999.00, 30),
  (1, 'Sony WH-1000XM5',     'Electronics', 29999.00, 75),
  (2, 'Kurti Set',           'Fashion',      1299.00, 200),
  (2, 'Denim Jacket',        'Fashion',      2499.00, 100),
  (3, 'Basmati Rice 5kg',    'Grocery',       599.00, 500),
  (3, 'Organic Dal 1kg',     'Grocery',       149.00, 800);

INSERT INTO customers (full_name, email, phone, address) VALUES
  ('Rahul Sharma', 'rahul@email.com', '9988776655', 'Delhi'),
  ('Priya Nair',   'priya@email.com', '8877665544', 'Bengaluru'),
  ('Amit Patel',   'amit@email.com',  '7766554433', 'Ahmedabad');

INSERT INTO orders (customer_id, product_id, quantity, total_price, status) VALUES
  (1, 1, 1, 79999.00, 'Delivered'),
  (1, 3, 2, 59998.00, 'Shipped'),
  (2, 4, 3,  3897.00, 'Confirmed'),
  (3, 2, 1, 54999.00, 'Pending');

SELECT 'Schema created and sample data inserted successfully!' AS Result;
