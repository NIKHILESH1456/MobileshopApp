package com.mobileshop.app.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.mobileshop.app.LoginActivity;
import com.mobileshop.app.R;

/**
 * Landing screen for the admin (the user whose email matches Constants.ADMIN_EMAIL).
 * From here the admin can add products, manage/edit/delete products,
 * change the shop logo, update the WhatsApp contact number, and view customer orders.
 */
public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btnAddProduct = findViewById(R.id.btnAddProduct);
        Button btnManageProducts = findViewById(R.id.btnManageProducts);
        Button btnChangeLogo = findViewById(R.id.btnChangeLogo);
        Button btnViewOrders = findViewById(R.id.btnViewOrders);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnAddProduct.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditProductActivity.class)));

        btnManageProducts.setOnClickListener(v ->
                startActivity(new Intent(this, ManageProductsActivity.class)));

        btnChangeLogo.setOnClickListener(v ->
                startActivity(new Intent(this, ChangeLogoActivity.class)));

        btnViewOrders.setOnClickListener(v ->
                startActivity(new Intent(this, AdminOrdersActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
