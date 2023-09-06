package com.example.diaryapp;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    final Context context;
    final ArrayList<Diary> diariesList;

    public DiaryAdapter(Context context, ArrayList<Diary> diariesList) {
        this.context = context;
        this.diariesList = diariesList;
    }

    public static class DiaryViewHolder extends RecyclerView.ViewHolder {

        private final TextView diaryTitle, diaryNote, diaryLocation;
        private final ImageView diaryImage;

        public DiaryViewHolder(@NonNull View itemView) {

            super(itemView);
            diaryTitle = itemView.findViewById(R.id.adapter_diary_title);
            diaryNote = itemView.findViewById(R.id.adapter_diary_note);
            diaryImage = itemView.findViewById(R.id.adapter_diary_image);
            diaryLocation = itemView.findViewById(R.id.adapter_diary_location);

        }

    }

    @NonNull
    @Override
    public DiaryAdapter.DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.card_diary, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryAdapter.DiaryViewHolder holder, int position) {

        Diary diary = diariesList.get(position);

        holder.diaryTitle.setText(diary.getTitle());
        holder.diaryNote.setText(diary.getNote());

        String diaryType = diary.getType();

        if (diaryType.equals("text")) {

            holder.diaryLocation.setVisibility(View.GONE);

            //default image
            Glide.with(context)
                    .load(R.drawable.login_background_image)
                    .transform(new RoundedCorners(15), new CenterCrop())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.diaryImage);

        } else if (diaryType.equals("image")) {

            holder.diaryLocation.setText(diary.getPlaceName());

            String imageURL = diary.getImage();

            if (!imageURL.isEmpty()) {
                Glide.with(context)
                        .load(imageURL)
                        .transform(new RoundedCorners(15), new CenterCrop())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(holder.diaryImage);
            }

        }

    }

    @Override
    public int getItemCount() {
        return diariesList.size();
    }

}
