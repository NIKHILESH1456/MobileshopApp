package com.mobileshop.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mobileshop.app.adapter.OrderAdapter;
import com.mobileshop.app.model.Order;
import com.mobileshop.app.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Shows the logged-in customer's own order history (read-only).
 */
public class OrdersActivity extends AppCompatActivity {

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

        adapter = new OrderAdapter(this, new ArrayList<>(), false, null);
        recyclerView.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference(Constants.NODE_ORDERS);
        Query query = ordersRef.orderByChild("userId").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
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
}
