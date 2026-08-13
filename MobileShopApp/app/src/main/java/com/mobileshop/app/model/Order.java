package com.mobileshop.app.model;

/**
 * Order model - created when a customer taps "Buy Now".
 * Stored under /orders/{orderId}. The admin can view and mark it complete
 * from the Admin Orders screen; actual payment/delivery is coordinated over WhatsApp.
 */
public class Order {

    private String orderId;
    private String userId;
    private String userEmail;
    private String userPhone;
    private String productId;
    private String productName;
    private double price;
    private String status; // "Pending", "Confirmed", "Delivered", "Cancelled"
    private long timestamp;

    public Order() {
        // Required empty constructor for Firebase
    }

    public Order(String orderId, String userId, String userEmail, String userPhone,
                 String productId, String productName, double price,
                 String status, long timestamp) {
        this.orderId = orderId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
