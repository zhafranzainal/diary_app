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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddDiaryActivity extends AppCompatActivity {

    private FirebaseUser mUser;
    private DatabaseReference mDatabaseRef;

    private final String mStoragePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final int mPermissionGranted = PackageManager.PERMISSION_GRANTED;

    private static final int STORAGE_REQUEST_CODE = 10;
    private static final int IMAGE_PICK_GALLERY_CODE = 20;

    private ImageView mDiaryImage;
    private TextInputEditText mDiaryTitle, mDiaryNote, mDiaryLocation;
    private MaterialButton mSaveDiaryButton;
    private CircularProgressIndicator mProgressIndicator;
    private Uri mSelectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://diary-app-fdcfc-default-rtdb.asia-southeast1.firebasedatabase.app");
        mDatabaseRef = firebaseDatabase.getReference();
        mUser = firebaseAuth.getCurrentUser();

        MaterialToolbar toolbar = findViewById(R.id.add_diary_toolbar);
        mDiaryTitle = findViewById(R.id.add_diary_title);
        mDiaryNote = findViewById(R.id.add_diary_note);
        mDiaryImage = findViewById(R.id.add_diary_image);
        mDiaryLocation = findViewById(R.id.add_diary_location);
        mSaveDiaryButton = findViewById(R.id.save_diary_button);
        mProgressIndicator = findViewById(R.id.add_diary_progress_bar);

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

        mSaveDiaryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String title = mDiaryTitle.getText().toString();
                String note = mDiaryNote.getText().toString();
                String location = mDiaryLocation.getText().toString();

                if (!title.isEmpty() && !note.isEmpty()) {

                    if (isTextDiary(location)) {
                        if (mUser != null) {
                            mSaveDiaryButton.setClickable(false);
                            mProgressIndicator.setVisibility(View.VISIBLE);
                            mProgressIndicator.setProgressCompat(500, true);
                            saveTextDiaryToDatabase(title, note, mUser.getUid());
                        }
                    } else if (isImageDiary(location)) {
                        if (mUser != null) {
                            mSaveDiaryButton.setClickable(false);
                            mProgressIndicator.setVisibility(View.VISIBLE);
                            mProgressIndicator.setProgressCompat(300, true);
                            saveImageDiaryToStorage(title, note, mSelectedImage, location, mUser.getUid());
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Please fill out all fields!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Please enter both title and note.", Toast.LENGTH_SHORT).show();
                }

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

                mSelectedImage = data.getData();

                Glide.with(this)
                        .load(mSelectedImage)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .transform(new CenterCrop())
                        .into(mDiaryImage);

                mDiaryImage.setVisibility(View.VISIBLE);
                mDiaryLocation.setVisibility(View.VISIBLE);
                mDiaryLocation.requestFocus();

            }

        }

    }

    private boolean isTextDiary(String location) {
        return mSelectedImage == null && location.isEmpty();
    }

    private boolean isImageDiary(String location) {
        return mSelectedImage != null && !location.isEmpty();
    }

    private void saveTextDiaryToDatabase(String title, String note, String userId) {

        HashMap<String, Object> diaryHashmap = new HashMap<>();
        diaryHashmap.put("title", title);
        diaryHashmap.put("note", note);
        diaryHashmap.put("type", "text");

        DatabaseReference diaryNode = mDatabaseRef.child(userId).push();
        String diaryNodeId = diaryNode.getKey();
        diaryHashmap.put("diaryId", diaryNodeId);

        mProgressIndicator.setVisibility(View.VISIBLE);
        mProgressIndicator.setProgressCompat(100, true);

        diaryNode.updateChildren(diaryHashmap).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    mProgressIndicator.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Diary added successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }

            }

        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                mSaveDiaryButton.setClickable(true);
            }

        });

    }

    private void saveImageDiaryToStorage(String title, String note, Uri selectedImage, String location, String userId) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        storageRef.child(userId).child(selectedImage.getLastPathSegment()).putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {

                    task.getResult().getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {

                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String imageURL = task.getResult().toString();
                                saveImageDiaryToDatabase(title, note, imageURL, location, userId);
                            }
                        }

                    }).addOnFailureListener(new OnFailureListener() {

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            mSaveDiaryButton.setClickable(true);
                        }

                    });

                }

            }

        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                mSaveDiaryButton.setClickable(true);
            }

        });

    }

    private void saveImageDiaryToDatabase(String title, String note, String imageURL, String location, String userId) {

        HashMap<String, Object> diaryHashmap = new HashMap<>();
        diaryHashmap.put("title", title);
        diaryHashmap.put("note", note);
        diaryHashmap.put("type", "image");
        diaryHashmap.put("image", imageURL);
        diaryHashmap.put("placeName", location);

        DatabaseReference diaryNode = mDatabaseRef.child(userId).push();
        String diaryNodeId = diaryNode.getKey();
        diaryHashmap.put("diaryId", diaryNodeId);

        mProgressIndicator.setVisibility(View.VISIBLE);
        mProgressIndicator.setProgressCompat(100, true);

        diaryNode.updateChildren(diaryHashmap).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    mProgressIndicator.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Diary added successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }

            }

        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                mSaveDiaryButton.setClickable(true);
            }

        });

    }

}