package com.mobileshop.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobileshop.app.R;
import com.mobileshop.app.model.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnStatusChangeListener {
        void onMarkConfirmed(Order order);
    }

    private final Context context;
    private List<Order> orders;
    private final boolean isAdminView;
    private final OnStatusChangeListener listener;

    public OrderAdapter(Context context, List<Order> orders, boolean isAdminView, OnStatusChangeListener listener) {
        this.context = context;
        this.orders = orders;
        this.isAdminView = isAdminView;
        this.listener = listener;
    }

    public void updateList(List<Order> newList) {
        this.orders = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        holder.tvProductName.setText(order.getProductName());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        holder.tvPrice.setText(format.format(order.getPrice()));

        holder.tvStatus.setText(order.getStatus());
        holder.tvDate.setText(new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(order.getTimestamp())));

        if (isAdminView) {
            holder.tvCustomer.setVisibility(View.VISIBLE);
            holder.tvCustomer.setText(order.getUserEmail());
            holder.btnConfirm.setVisibility("Pending".equals(order.getStatus()) ? View.VISIBLE : View.GONE);
            holder.btnConfirm.setOnClickListener(v -> {
                if (listener != null) listener.onMarkConfirmed(order);
            });
        } else {
            holder.tvCustomer.setVisibility(View.GONE);
            holder.btnConfirm.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvPrice, tvStatus, tvDate, tvCustomer;
        Button btnConfirm;

        OrderViewHolder(View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
        }
    }
}
