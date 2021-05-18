package com.reactnativemediapicker.adapters;

import android.content.ContentUris;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.reactnativemediapicker.R;
import com.reactnativemediapicker.holders.AlbumViewHolder;
import com.reactnativemediapicker.interfaces.AlbumClickEvent;
import com.reactnativemediapicker.models.Album;

import java.util.ArrayList;

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumViewHolder> {

  private ArrayList<Album> mAlbums;
  public AlbumClickEvent event;
  private final RequestBuilder<Drawable> mRequestBuilder;
  private Resources res;

  public AlbumListAdapter(ArrayList<Album> albums, RequestManager requestManager, Resources resources) {
    this.mAlbums = albums;
    this.mRequestBuilder = requestManager.asDrawable();
    this.res = resources;
  }

  @Override
  public int getItemViewType(final int position) {
    return R.layout.album_frame;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @NonNull
  @Override
  public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_frame, parent, false);
    return new AlbumViewHolder(view);
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
    Album album = this.mAlbums.get(position);
    holder.title.setText(album.getTitle());
    holder.total.setText(String.format(res.getString(R.string.number_of_photos), String.valueOf(album.getSize())));
    holder.index = position;
    holder.event = event;

    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, album.getThumbnailId());
    mRequestBuilder.load(uri)
      .transform(new CenterCrop(), new RoundedCorners(16))
      .into(holder.thumbnail);
  }

  @Override
  public int getItemCount() {
    return mAlbums.size();
  }
}
