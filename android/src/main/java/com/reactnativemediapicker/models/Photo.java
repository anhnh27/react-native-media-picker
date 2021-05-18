package com.reactnativemediapicker.models;

import android.net.Uri;

public class Photo {
  private int _id;
  private Uri _uri;

  public Photo(int id, Uri uri) {
    _id = id;
    _uri = uri;
  }

  public int getId() {
    return _id;
  }

  public Uri getUri() {
    return _uri;
  }
}
