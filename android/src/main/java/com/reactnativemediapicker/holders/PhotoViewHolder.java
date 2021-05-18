package com.reactnativemediapicker.holders;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.reactnativemediapicker.R;
import com.reactnativemediapicker.fragments.ImagePickerFragment;
import com.reactnativemediapicker.interfaces.PhotoClickEvent;

public class PhotoViewHolder extends RecyclerView.ViewHolder {
  public PhotoClickEvent event;
  public ImageView thumbnail;
  public ImageView checkmark;
  public int position;

  public PhotoViewHolder(View itemView) {
    super(itemView);
    checkmark = itemView.findViewById(R.id.checkmark);
    thumbnail = itemView.findViewById(R.id.photo_thumbnail);
    thumbnail.setOnClickListener((View v) -> {
      event.onPhotoClick(position);
      setSelectedBG(ImagePickerFragment.selectedPhotos.contains(position));
    });
  }

  public void setSelectedBG(boolean selected) {
    thumbnail.setAlpha(selected ? 0.5f : 1.0f);
    checkmark.setVisibility(selected ? View.VISIBLE : View.GONE);
  }
}

