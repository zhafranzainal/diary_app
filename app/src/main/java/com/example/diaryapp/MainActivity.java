package com.example.diaryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RecyclerView mRecyclerView;
    private LottieAnimationView mDiaryAnimationView;
    private TextView mNoDiaryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.main_activity_toolbar);
        ExtendedFloatingActionButton addDiaryButton = findViewById(R.id.add_diary_button);
        mRecyclerView = findViewById(R.id.diary_list_recycler_view);
        mDiaryAnimationView = findViewById(R.id.diary_lottie_animation);
        mNoDiaryTextView = findViewById(R.id.no_diary_text_view);

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(horizontalLayoutManager);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            getDiaries(user.getUid());
        }

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

    private void getDiaries(String userId) {

        ArrayList<Diary> diariesList = new ArrayList<>();
        DiaryAdapter diaryAdapter = new DiaryAdapter(getApplicationContext(), diariesList);
        mRecyclerView.setAdapter(diaryAdapter);

        // get reference to root node of database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        // create child reference under root node to access specific user data
        databaseRef.child(userId).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // if diary entries exist
                if (snapshot.exists()) {

                    // clear diary list before looping to avoid item duplication
                    diariesList.clear();

                    // loop through all existing diaries in snapshot
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Diary diary = snap.getValue(Diary.class);
                        diariesList.add(diary);
                    }

                    // display latest diaries
                    Collections.reverse(diariesList);
                    diaryAdapter.notifyDataSetChanged();

                } else {

                    // if no diary yet, display diary animation with 'no diary' text
                    mDiaryAnimationView.setVisibility(View.VISIBLE);
                    mNoDiaryTextView.setVisibility(View.VISIBLE);
                    mDiaryAnimationView.playAnimation();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }

}