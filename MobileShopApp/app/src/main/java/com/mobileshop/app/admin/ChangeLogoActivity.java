package com.mobileshop.app.admin;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.mobileshop.app.R;
import com.mobileshop.app.utils.Constants;

/**
 * Lets the admin replace the shop logo (shown in the customer app toolbar)
 * and update the WhatsApp contact number used by the "Contact Admin" button everywhere.
 */
public class ChangeLogoActivity extends AppCompatActivity {

    private ImageView ivLogoPreview;
    private EditText etWhatsappNumber;
    private Button btnSelectLogo, btnUploadLogo, btnSaveNumber;
    private ProgressBar progressBar;

    private Uri selectedLogoUri;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedLogoUri = uri;
                    Glide.with(this).load(uri).into(ivLogoPreview);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_logo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        ivLogoPreview = findViewById(R.id.ivLogoPreview);
        etWhatsappNumber = findViewById(R.id.etWhatsappNumber);
        btnSelectLogo = findViewById(R.id.btnSelectLogo);
        btnUploadLogo = findViewById(R.id.btnUploadLogo);
        btnSaveNumber = findViewById(R.id.btnSaveNumber);
        progressBar = findViewById(R.id.progressBar);

        loadCurrentSettings();

        btnSelectLogo.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnUploadLogo.setOnClickListener(v -> uploadLogo());
        btnSaveNumber.setOnClickListener(v -> saveWhatsappNumber());
    }

    private void loadCurrentSettings() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.NODE_SETTINGS);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String logoUrl = snapshot.child(Constants.SETTINGS_LOGO_URL).getValue(String.class);
                String number = snapshot.child(Constants.SETTINGS_WHATSAPP_NUMBER).getValue(String.class);
                if (logoUrl != null) {
                    Glide.with(ChangeLogoActivity.this).load(logoUrl).into(ivLogoPreview);
                }
                if (number != null) {
                    etWhatsappNumber.setText(number);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void uploadLogo() {
        if (selectedLogoUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading(true);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child(Constants.STORAGE_LOGO).child("logo.jpg");

        storageRef.putFile(selectedLogoUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) throw task.getException();
                    return storageRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    FirebaseDatabase.getInstance().getReference(Constants.NODE_SETTINGS)
                            .child(Constants.SETTINGS_LOGO_URL).setValue(downloadUri.toString())
                            .addOnCompleteListener(task -> {
                                showLoading(false);
                                Toast.makeText(this, "Logo updated", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveWhatsappNumber() {
        String number = etWhatsappNumber.getText().toString().trim();
        if (TextUtils.isEmpty(number)) {
            Toast.makeText(this, "Enter a WhatsApp number with country code", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseDatabase.getInstance().getReference(Constants.NODE_SETTINGS)
                .child(Constants.SETTINGS_WHATSAPP_NUMBER).setValue(number)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "WhatsApp number updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnUploadLogo.setEnabled(!loading);
    }
}
