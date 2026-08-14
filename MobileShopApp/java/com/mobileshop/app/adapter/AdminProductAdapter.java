package com.mobileshop.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mobileshop.app.R;
import com.mobileshop.app.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    public interface OnAdminProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    private final Context context;
    private List<Product> products;
    private final OnAdminProductActionListener listener;

    public AdminProductAdapter(Context context, List<Product> products, OnAdminProductActionListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    public void updateList(List<Product> newList) {
        this.products = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvName.setText(product.getName());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        holder.tvPrice.setText(format.format(product.getPrice()));
        holder.tvStock.setText(product.isInStock() ? "In Stock" : "Out of Stock");

        Glide.with(context).load(product.getImageUrl())
                .placeholder(R.drawable.ic_placeholder).into(holder.ivImage);

        holder.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(product); });
        holder.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(product); });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvStock, btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStock = itemView.findViewById(R.id.tvStock);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
