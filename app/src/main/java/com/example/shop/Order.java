package com.example.shop;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Order {
    private int id;
    private int customer_id;  // Sửa từ customerId thành customer_id
    private int product_id;   // Sửa từ productId thành product_id
    private int quantity;
    private double total_price; // Sửa từ totalPrice thành total_price
    private String order_date;  // Sửa từ orderDate thành order_date
    private String status;

    // Constructor
    public Order(int customer_id, int product_id, int quantity, double total_price) {
        this.customer_id = customer_id;
        this.product_id = product_id;
        this.quantity = quantity;
        this.total_price = total_price;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomer_id() { return customer_id; }
    public void setCustomer_id(int customer_id) { this.customer_id = customer_id; }

    public int getProduct_id() { return product_id; }
    public void setProduct_id(int product_id) { this.product_id = product_id; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotal_price() { return total_price; }
    public void setTotal_price(double total_price) { this.total_price = total_price; }

    public String getOrder_date() { return order_date; }
    public void setOrder_date(String order_date) { this.order_date = order_date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}