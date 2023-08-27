package com.example.diaryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

public class AddDiaryActivity extends AppCompatActivity {

    private final String mStoragePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final int mPermissionGranted = PackageManager.PERMISSION_GRANTED;

    private static final int STORAGE_REQUEST_CODE = 10;
    private static final int IMAGE_PICK_GALLERY_CODE = 20;

    private ImageView mDiaryImage;
    private TextInputEditText mDiaryLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary);

        MaterialToolbar toolbar = findViewById(R.id.add_diary_toolbar);
        mDiaryImage = findViewById(R.id.add_diary_image);
        mDiaryLocation = findViewById(R.id.add_diary_location);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

        });

        toolbar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                displayOptionBuilder();
                return false;
            }

        });

    }

    private void displayOptionBuilder() {

        String options[] = {"Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }

        });

        builder.create().show();

    }

    private boolean checkStoragePermission() {
        int storagePermission = ContextCompat.checkSelfPermission(this, mStoragePermission);
        return storagePermission == mPermissionGranted;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{mStoragePermission}, STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == mPermissionGranted) {
                pickFromGallery();
            } else {
                requestStoragePermission();
            }

        }

    }

    private void pickFromGallery() {
        Intent implicitIntent = new Intent(Intent.ACTION_PICK);
        implicitIntent.setType("image/*");
        startActivityForResult(implicitIntent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK) {

            if (data != null) {

                Uri selectedImage = data.getData();

                Glide.with(this)
                        .load(selectedImage)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .transform(new CenterCrop())
                        .into(mDiaryImage);

                mDiaryImage.setVisibility(View.VISIBLE);
                mDiaryLocation.setVisibility(View.VISIBLE);
                mDiaryLocation.requestFocus();

            }

        }

    }

}