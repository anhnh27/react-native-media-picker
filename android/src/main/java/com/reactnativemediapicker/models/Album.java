package com.reactnativemediapicker.models;

import android.net.Uri;

public class Album {
  private int _albumId;
  private int _thumbnailId;
  private String _albumTitle;
  private Uri _albumThumbnailUri;
  private int _size;

  public int getId() {
    return _albumId;
  }

  public int getThumbnailId() {
    return _thumbnailId;
  }

  public String getTitle() {
    return _albumTitle;
  }

  public Uri getThumbnailUri() {
    return _albumThumbnailUri;
  }

  public int getSize() {
    return _size;
  }

  public Album(int albumId,
               String albumTitle,
               int thumbnailId,
               Uri albumThumbnailUri,
               int size) {
    _albumId = albumId;
    _albumTitle = albumTitle;
    _albumThumbnailUri = albumThumbnailUri;
    _thumbnailId = thumbnailId;
    _size = size;
  }
}
