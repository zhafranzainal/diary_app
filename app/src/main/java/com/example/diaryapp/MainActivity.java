package com.example.diaryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        MaterialToolbar toolbar = findViewById(R.id.main_activity_toolbar);
        ExtendedFloatingActionButton addDiaryButton = findViewById(R.id.add_diary_button);

        toolbar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {

                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    mAuth.signOut();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                return true;
            }

        });

        addDiaryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    startActivity(new Intent(getApplicationContext(), AddDiaryActivity.class));
                }

            }

        });

    }

}