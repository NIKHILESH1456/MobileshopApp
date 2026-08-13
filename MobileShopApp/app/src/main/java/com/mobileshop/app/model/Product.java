package com.mobileshop.app.model;

/**
 * Product model - represents one mobile accessory item in the shop.
 * This is stored as a JSON object under /products/{productId} in Firebase Realtime Database.
 */
public class Product {

    private String productId;
    private String name;
    private String description;
    private double price;
    private String category;      // e.g. "Cable", "Charger", "Earphones", "Cover", "Screen Guard"
    private String imageUrl;      // Firebase Storage download URL
    private String videoUrl;      // Optional: YouTube link or direct video URL
    private boolean inStock;
    private long timestamp;

    public Product() {
        // Required empty constructor for Firebase
    }

    public Product(String productId, String name, String description, double price,
                    String category, String imageUrl, String videoUrl,
                    boolean inStock, long timestamp) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.inStock = inStock;
        this.timestamp = timestamp;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
