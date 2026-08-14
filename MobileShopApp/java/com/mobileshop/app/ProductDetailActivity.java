package com.mobileshop.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobileshop.app.model.Order;
import com.mobileshop.app.model.Product;
import com.mobileshop.app.utils.Constants;
import com.mobileshop.app.utils.WhatsAppHelper;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProduct;
    private TextView tvName, tvPrice, tvCategory, tvDescription, tvStock;
    private Button btnBuyNow;
    private FloatingActionButton fabWhatsApp;

    private Product currentProduct;
    private String whatsappNumber = Constants.DEFAULT_WHATSAPP_NUMBER;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ivProduct = findViewById(R.id.ivProduct);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvCategory = findViewById(R.id.tvCategory);
        tvDescription = findViewById(R.id.tvDescription);
        tvStock = findViewById(R.id.tvStock);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        fabWhatsApp = findViewById(R.id.fabWhatsApp);

        String productId = getIntent().getStringExtra("productId");
        loadSettings();
        loadProduct(productId);

        btnBuyNow.setOnClickListener(v -> placeOrder());
        fabWhatsApp.setOnClickListener(v -> {
            String msg = currentProduct != null
                    ? "Hi, I have a question about: " + currentProduct.getName()
                    : "Hi, I have a question about a product.";
            WhatsAppHelper.openChat(this, whatsappNumber, msg);
        });
    }

    private void loadSettings() {
        FirebaseDatabase.getInstance().getReference(Constants.NODE_SETTINGS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String number = snapshot.child(Constants.SETTINGS_WHATSAPP_NUMBER).getValue(String.class);
                        if (number != null && !number.isEmpty()) whatsappNumber = number;
                    }

                    @Override
                    public void onCancelled(DatabaseError error) { }
                });
    }

    private void loadProduct(String productId) {
        if (TextUtils.isEmpty(productId)) {
            finish();
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(Constants.NODE_PRODUCTS).child(productId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product == null) {
                    Toast.makeText(ProductDetailActivity.this, "Product not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentProduct = product;
                bindProduct(product);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProductDetailActivity.this, "Failed to load product", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindProduct(Product product) {
        tvName.setText(product.getName());
        tvCategory.setText(product.getCategory());
        tvDescription.setText(product.getDescription());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        tvPrice.setText(format.format(product.getPrice()));

        Glide.with(this).load(product.getImageUrl())
                .placeholder(R.drawable.ic_placeholder).into(ivProduct);

        if (product.isInStock()) {
            tvStock.setText("In Stock");
            tvStock.setTextColor(getResources().getColor(R.color.whatsapp_green));
            btnBuyNow.setEnabled(true);
            btnBuyNow.setText(getString(R.string.buy_now));
        } else {
            tvStock.setText("Out of Stock — contact admin on WhatsApp");
            tvStock.setTextColor(getResources().getColor(R.color.red));
            btnBuyNow.setEnabled(false);
            btnBuyNow.setText("Currently Unavailable");
        }
    }

    private void placeOrder() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || currentProduct == null) return;

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference(Constants.NODE_ORDERS);
        String orderId = ordersRef.push().getKey();
        if (orderId == null) return;

        Order order = new Order(
                orderId,
                user.getUid(),
                user.getEmail(),
                "",
                currentProduct.getProductId(),
                currentProduct.getName(),
                currentProduct.getPrice(),
                "Pending",
                System.currentTimeMillis()
        );

        ordersRef.child(orderId).setValue(order).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Order placed! Confirm details on WhatsApp.", Toast.LENGTH_LONG).show();
                String msg = "Hi, I just placed an order in the app:\n"
                        + "Product: " + currentProduct.getName() + "\n"
                        + "Price: " + currentProduct.getPrice() + "\n"
                        + "Please confirm and share delivery details.";
                WhatsAppHelper.openChat(this, whatsappNumber, msg);
            } else {
                Toast.makeText(this, "Failed to place order. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
