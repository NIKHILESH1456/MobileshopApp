package com.mobileshop.app.admin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

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
import com.mobileshop.app.adapter.OrderAdapter;
import com.mobileshop.app.model.Order;
import com.mobileshop.app.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Admin-only screen listing every order placed by every customer,
 * with a button to mark a pending order as Confirmed once the admin
 * has coordinated payment/delivery over WhatsApp.
 */
public class AdminOrdersActivity extends AppCompatActivity implements OrderAdapter.OnStatusChangeListener {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OrderAdapter(this, new ArrayList<>(), true, this);
        recyclerView.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference(Constants.NODE_ORDERS);
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Order> orders = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Order order = child.getValue(Order.class);
                    if (order != null) orders.add(order);
                }
                Collections.sort(orders, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                adapter.updateList(orders);
                tvEmpty.setVisibility(orders.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    @Override
    public void onMarkConfirmed(Order order) {
        FirebaseDatabase.getInstance().getReference(Constants.NODE_ORDERS)
                .child(order.getOrderId()).child("status").setValue("Confirmed")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Order marked as confirmed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
