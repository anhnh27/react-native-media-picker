package com.reactnativemediapicker.adapters;

import android.content.ContentUris;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.reactnativemediapicker.R;
import com.reactnativemediapicker.fragments.ImagePickerFragment;
import com.reactnativemediapicker.holders.PhotoViewHolder;
import com.reactnativemediapicker.interfaces.PhotoClickEvent;
import com.reactnativemediapicker.models.Photo;

import java.util.ArrayList;
import java.util.List;

public class PhotoListAdapter extends RecyclerView.Adapter<PhotoViewHolder> implements ListPreloader.PreloadModelProvider<Photo>, ListPreloader.PreloadSizeProvider<Photo> {

  private final int VIEW_SIZE = Resources.getSystem().getDisplayMetrics().widthPixels / 3;
  private ArrayList<Photo> mPhotos;
  private final RequestBuilder<Drawable> mRequestBuilder;
  public PhotoClickEvent event;
  private int[] actualDimensions;

  public PhotoListAdapter(RequestManager requestManager, ArrayList<Photo> photos) {
    this.mPhotos = photos;
    this.mRequestBuilder = requestManager
      .asDrawable()
      .apply(RequestOptions.fitCenterTransform());
  }

  @Override
  public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_frame, parent, false);
    view.setLayoutParams(new ViewGroup.LayoutParams(VIEW_SIZE, VIEW_SIZE));
    PhotoViewHolder holder = new PhotoViewHolder(view);
    holder.event = event;
    actualDimensions = new int[]{VIEW_SIZE, VIEW_SIZE};
    return holder;
  }

  @Override
  public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
    Photo photo = this.mPhotos.get(position);
    holder.setSelectedBG(ImagePickerFragment.selectedPhotos.contains(position));
    holder.position = position;

    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, photo.getId());
    mRequestBuilder
      .load(uri)
      .override(VIEW_SIZE, VIEW_SIZE)
      .into(holder.thumbnail);
  }

  @Override
  public int getItemCount() {
    return mPhotos.size();
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public int getItemViewType(int position) {
    return position;
  }


  @NonNull
  @Override
  public List<Photo> getPreloadItems(int position) {
    if (position < mPhotos.size() - 3) {
      return mPhotos.subList(position, position + 3);
    } else {
      return mPhotos.subList(position, mPhotos.size() - 1);
    }
  }

  @Nullable
  @Override
  public RequestBuilder<Drawable> getPreloadRequestBuilder(@NonNull Photo item) {
    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, item.getId());
    return mRequestBuilder
      .override(VIEW_SIZE, VIEW_SIZE)
      .load(uri);
  }

  @Nullable
  @Override
  public int[] getPreloadSize(@NonNull Photo item, int adapterPosition, int perItemPosition) {
    return actualDimensions;
  }
}
