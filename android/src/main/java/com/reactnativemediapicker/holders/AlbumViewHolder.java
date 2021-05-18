package com.reactnativemediapicker.holders;

import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.reactnativemediapicker.R;
import com.reactnativemediapicker.interfaces.AlbumClickEvent;

public class AlbumViewHolder extends RecyclerView.ViewHolder {

  public int index;
  public AlbumClickEvent event;
  public TextView title;
  public TextView total;
  public ImageView thumbnail;

  public AlbumViewHolder(View itemView) {
    super(itemView);
    itemView.setOnClickListener(this::onClick);
    title = itemView.findViewById(R.id.album_title);
    title.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);
    total = itemView.findViewById(R.id.total);
    total.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);
    thumbnail = itemView.findViewById(R.id.photo_thumbnail);
  }

  private void onClick(View v) {
    event.onAlbumClick(index);
  }
}
