package com.mobileshop.app.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobileshop.app.R;
import com.mobileshop.app.adapter.AdminProductAdapter;
import com.mobileshop.app.model.Product;
import com.mobileshop.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ManageProductsActivity extends AppCompatActivity implements AdminProductAdapter.OnAdminProductActionListener {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private AdminProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminProductAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.NODE_PRODUCTS);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Product> products = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Product p = child.getValue(Product.class);
                    if (p != null) products.add(p);
                }
                adapter.updateList(products);
                tvEmpty.setVisibility(products.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    @Override
    public void onEdit(Product product) {
        Intent intent = new Intent(this, AddEditProductActivity.class);
        intent.putExtra("productId", product.getProductId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \"" + product.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference(Constants.NODE_PRODUCTS)
                            .child(product.getProductId()).removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
