from flask import Flask, request, jsonify
import sqlite3
from datetime import datetime

app = Flask(__name__)

# Database connection
def get_db():
    conn = sqlite3.connect('shop.db')
    conn.row_factory = sqlite3.Row
    return conn

# Initialize database
def init_db():
    conn = get_db()
    cur = conn.cursor()
    
    # Create tables
    cur.execute('''
    CREATE TABLE IF NOT EXISTS products (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        price REAL NOT NULL,
        image TEXT,
        description TEXT
    )
    ''')

    cur.execute('''
    CREATE TABLE IF NOT EXISTS customers (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        phone TEXT NOT NULL,
        address TEXT NOT NULL
    )
    ''')

    cur.execute('''
    CREATE TABLE IF NOT EXISTS orders (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        customer_id INTEGER,
        customer_name TEXT NOT NULL,
        phone TEXT NOT NULL,
        address TEXT NOT NULL,
        product_id INTEGER,
        product_name TEXT NOT NULL,
        product_image TEXT,
        quantity INTEGER NOT NULL,
        total_price REAL NOT NULL,
        order_date TEXT NOT NULL,
        status TEXT DEFAULT 'Đang xử lý',
        FOREIGN KEY (customer_id) REFERENCES customers(id),
        FOREIGN KEY (product_id) REFERENCES products(id)
    )
    ''')

    # Insert sample data
    cur.execute('SELECT COUNT(*) FROM products')
    if cur.fetchone()[0] == 0:
        cur.execute('''
        INSERT INTO products (name, price, image, description) VALUES 
        ("Áo thun nam", 199000, "https://pos.nvncdn.com/2865a9-85186/ps/20240409_MduYE64Evl.jpeg", "Áo thun nam cao cấp"),
        ("Áo sơ mi nữ", 299000, "https://pos.nvncdn.com/2865a9-85186/ps/20231129_thsH3ue8xt.jpeg", "Áo sơ mi nữ thời trang"),
        ("Áo vest", 2990000, "https://product.hstatic.net/1000333436/product/av348_f38d712d83484212b0223b1c76b1816f_master.jpg", "Áo vest đẳng cấp thời thượng"),
        ("Quần jean", 399000, "https://pos.nvncdn.com/2865a9-85186/ps/20240613_L3McR9IPfO.jpeg", "Quần jean cao cấp")
        ''')

    conn.commit()
    conn.close()

# Initialize database
init_db()

@app.route('/')
def home():
    return {
        "message": "Shop API",
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "author": "tuangato147"
    }

@app.route('/api/products', methods=['GET'])
def get_products():
    db = get_db()
    products = db.execute('SELECT * FROM products').fetchall()
    return jsonify([dict(row) for row in products])

@app.route('/api/products', methods=['POST'])
def create_product():
    data = request.get_json()
    if not data or not all(key in data for key in ['name', 'price','image']):
        return jsonify({'error': 'Missing required fields'}), 400

    db = get_db()
    cursor = db.cursor()
    cursor.execute(
        'INSERT INTO products (name, price, image, description) VALUES (?, ?, ?, ?)',
        (data['name'], data['price'], data.get('image'), data.get('description'))
    )
    db.commit()
    return jsonify({'id': cursor.lastrowid, **data})

@app.route('/api/customers', methods=['POST'])
def create_customer():
    data = request.get_json()
    if not data or not all(key in data for key in ['name', 'phone', 'address']):
        return jsonify({'error': 'Missing required fields'}), 400

    db = get_db()
    cursor = db.cursor()
    
    try:
        cursor.execute(
            'INSERT INTO customers (name, phone, address) VALUES (?, ?, ?)',
            (data['name'], data['phone'], data['address'])
        )
        db.commit()
        
        # Lấy customer vừa tạo
        created_customer = cursor.execute(
            'SELECT * FROM customers WHERE id = ?', 
            (cursor.lastrowid,)
        ).fetchone()
        
        return jsonify(dict(created_customer))
    except Exception as e:
        db.rollback()
        return jsonify({'error': str(e)}), 500

@app.route('/api/orders', methods=['GET'])
def get_orders():
    db = get_db()
    orders = db.execute('''
        SELECT orders.*, customers.name as customer_name, customers.phone, customers.address,
               products.name as product_name, products.image as product_image
        FROM orders
        JOIN customers ON orders.customer_id = customers.id
        JOIN products ON orders.product_id = products.id
        ORDER BY orders.order_date DESC
    ''').fetchall()
    return jsonify([dict(row) for row in orders])

@app.route('/api/orders', methods=['POST'])
def create_order():
    data = request.get_json()
    if not data or not all(key in data for key in ['customer_id', 'product_id', 'quantity', 'total_price']):
        return jsonify({'error': 'Missing required fields'}), 400

    db = get_db()
    cursor = db.cursor()

    try:
        # Lấy thông tin khách hàng
        customer = cursor.execute(
            'SELECT * FROM customers WHERE id = ?', 
            (data['customer_id'],)
        ).fetchone()
        if not customer:
            return jsonify({'error': 'Customer not found'}), 404

        # Lấy thông tin sản phẩm
        product = cursor.execute(
            'SELECT * FROM products WHERE id = ?', 
            (data['product_id'],)
        ).fetchone()
        if not product:
            return jsonify({'error': 'Product not found'}), 404

        now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        
        # Tạo đơn hàng với đầy đủ thông tin
        cursor.execute('''
            INSERT INTO orders (
                customer_id, customer_name, phone, address,
                product_id, product_name, product_image,
                quantity, total_price, order_date, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            data['customer_id'],
            customer['name'],
            customer['phone'],
            customer['address'],
            data['product_id'],
            product['name'],
            product['image'],
            data['quantity'],
            data['total_price'],
            now,
            'Đang xử lý'
        ))
        
        db.commit()
        
        # Trả về thông tin đơn hàng đầy đủ
        order = cursor.execute(
            'SELECT * FROM orders WHERE id = ?', 
            (cursor.lastrowid,)
        ).fetchone()
        
        return jsonify(dict(order))

    except Exception as e:
        db.rollback()
        return jsonify({'error': str(e)}), 500

@app.route('/api/orders/<int:order_id>', methods=['PUT'])
def update_order(order_id):
    data = request.get_json()
    if not data or 'status' not in data:
        return jsonify({'error': 'Missing status field'}), 400

    db = get_db()
    cursor = db.cursor()
    
    try:
        cursor.execute(
            'UPDATE orders SET status = ? WHERE id = ?',
            (data['status'], order_id)
        )
        db.commit()
        
        order = cursor.execute(
            'SELECT * FROM orders WHERE id = ?', 
            (order_id,)
        ).fetchone()
        
        if order:
            return jsonify(dict(order))
        return jsonify({'error': 'Order not found'}), 404

    except Exception as e:
        db.rollback()
        return jsonify({'error': str(e)}), 500

@app.after_request
def after_request(response):
    response.headers.add('Access-Control-Allow-Origin', '*')
    response.headers.add('Access-Control-Allow-Headers', 'Content-Type,Authorization')
    response.headers.add('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE')
    return response

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000, debug=True)