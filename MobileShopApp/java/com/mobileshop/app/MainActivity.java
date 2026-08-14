package com.mobileshop.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobileshop.app.adapter.ProductAdapter;
import com.mobileshop.app.model.Product;
import com.mobileshop.app.notifications.NotificationHelper;
import com.mobileshop.app.utils.Constants;
import com.mobileshop.app.utils.WhatsAppHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmpty;
    private ImageView ivLogo;
    private FloatingActionButton fabWhatsApp;

    private ProductAdapter adapter;
    private final List<Product> allProducts = new ArrayList<>();

    private String whatsappNumber = Constants.DEFAULT_WHATSAPP_NUMBER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ivLogo = findViewById(R.id.ivLogo);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabWhatsApp = findViewById(R.id.fabWhatsApp);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Regular users get subscribed to the "new_products" alert topic the moment
        // they land on the home screen, so they're notified whenever the admin adds a phone.
        NotificationHelper.subscribeUserToProductAlerts(this);

        swipeRefreshLayout.setOnRefreshListener(this::loadProducts);
        fabWhatsApp.setOnClickListener(v -> WhatsAppHelper.openChat(this, whatsappNumber,
                "Hi, I'm looking for a mobile accessory that I couldn't find in the app. Can you help?"));

        loadSettings();
        loadProducts();
    }

    private void loadSettings() {
        DatabaseReference settingsRef = FirebaseDatabase.getInstance().getReference(Constants.NODE_SETTINGS);
        settingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String logoUrl = snapshot.child(Constants.SETTINGS_LOGO_URL).getValue(String.class);
                String number = snapshot.child(Constants.SETTINGS_WHATSAPP_NUMBER).getValue(String.class);

                if (logoUrl != null && !logoUrl.isEmpty()) {
                    Glide.with(MainActivity.this).load(logoUrl)
                            .placeholder(R.drawable.ic_placeholder).into(ivLogo);
                }
                if (number != null && !number.isEmpty()) {
                    whatsappNumber = number;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void loadProducts() {
        swipeRefreshLayout.setRefreshing(true);
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference(Constants.NODE_PRODUCTS);
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allProducts.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Product product = child.getValue(Product.class);
                    if (product != null) {
                        allProducts.add(product);
                    }
                }
                adapter.updateList(allProducts);
                tvEmpty.setVisibility(allProducts.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
        return true;
    }

    private void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.updateList(allProducts);
            return;
        }
        String q = query.toLowerCase(Locale.getDefault());
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if ((p.getName() != null && p.getName().toLowerCase(Locale.getDefault()).contains(q))
                    || (p.getCategory() != null && p.getCategory().toLowerCase(Locale.getDefault()).contains(q))) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_my_orders) {
            startActivity(new Intent(this, OrdersActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("productId", product.getProductId());
        startActivity(intent);
    }
}
