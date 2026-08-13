package com.mobileshop.app.admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobileshop.app.R;
import com.mobileshop.app.model.Product;
import com.mobileshop.app.utils.Constants;

import java.util.UUID;

/**
 * Used both to ADD a new product and to EDIT an existing one.
 * If the intent extra "productId" is present, the screen loads that product for editing;
 * otherwise it creates a brand-new product.
 */
public class AddEditProductActivity extends AppCompatActivity {

    private ImageView ivPreview;
    private EditText etName, etDescription, etPrice, etCategory, etVideoUrl;
    private CheckBox cbInStock;
    private Button btnSelectImage, btnSave;
    private ProgressBar progressBar;

    private Uri selectedImageUri;
    private String existingImageUrl;
    private String productId;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).into(ivPreview);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        ivPreview = findViewById(R.id.ivPreview);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etCategory = findViewById(R.id.etCategory);
        etVideoUrl = findViewById(R.id.etVideoUrl);
        cbInStock = findViewById(R.id.cbInStock);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        cbInStock.setChecked(true);

        btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> saveProduct());

        productId = getIntent().getStringExtra("productId");
        if (!TextUtils.isEmpty(productId)) {
            setTitle("Edit Product");
            loadExistingProduct(productId);
        } else {
            setTitle(getString(R.string.add_product));
        }
    }

    private void loadExistingProduct(String id) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(Constants.NODE_PRODUCTS).child(id);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product == null) return;
                etName.setText(product.getName());
                etDescription.setText(product.getDescription());
                etPrice.setText(String.valueOf(product.getPrice()));
                etCategory.setText(product.getCategory());
                etVideoUrl.setText(product.getVideoUrl());
                cbInStock.setChecked(product.isInStock());
                existingImageUrl = product.getImageUrl();
                if (existingImageUrl != null) {
                    Glide.with(AddEditProductActivity.this).load(existingImageUrl).into(ivPreview);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String videoUrl = etVideoUrl.getText().toString().trim();
        boolean inStock = cbInStock.isChecked();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "Product name and price are required", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid price", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        if (selectedImageUri != null) {
            uploadImageThenSave(name, description, price, category, videoUrl, inStock);
        } else {
            persistProduct(name, description, price, category, videoUrl, inStock, existingImageUrl);
        }
    }

    private void uploadImageThenSave(String name, String description, double price,
                                      String category, String videoUrl, boolean inStock) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child(Constants.STORAGE_PRODUCT_IMAGES).child(fileName);

        storageRef.putFile(selectedImageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        throw task.getException();
                    }
                    return storageRef.getDownloadUrl();
                })
                .addOnSuccessListener((Uri downloadUri) ->
                        persistProduct(name, description, price, category, videoUrl, inStock, downloadUri.toString()))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void persistProduct(String name, String description, double price, String category,
                                 String videoUrl, boolean inStock, String imageUrl) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference(Constants.NODE_PRODUCTS);
        String id = TextUtils.isEmpty(productId) ? productsRef.push().getKey() : productId;
        if (id == null) {
            showLoading(false);
            return;
        }

        Product product = new Product(id, name, description, price, category,
                imageUrl, videoUrl, inStock, System.currentTimeMillis());

        productsRef.child(id).setValue(product).addOnCompleteListener(task -> {
            showLoading(false);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Product saved", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
    }
}
