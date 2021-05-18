package com.reactnativemediapicker.helpers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public final class AlbumQuery {
  @NonNull
  public static ArrayList<Album> get(@NonNull final Context context) {
    final ArrayList<Album> output = new ArrayList<>();
    final ArrayList<String> bucketIdArr = new ArrayList<>();
    final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    final String[] projection = {MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};

    try (final Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null)) {
      if ((cursor != null) && (cursor.moveToFirst() == true)) {
        final int columnBucketName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        final int columnBucketId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
        final int columnBucketThumbnail = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        final int columnBucketThumbnailId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

        do {
          final String bucketId = cursor.getString(columnBucketId);
          final String bucketName = cursor.getString(columnBucketName);
          final Uri bucketThumbnail = Uri.parse(cursor.getString(columnBucketThumbnail));
          final int bucketThumbnailId = cursor.getInt(columnBucketThumbnailId);

          if (bucketIdArr.contains(bucketId) == false) {
            final int count = AlbumQuery.getCount(context, contentUri, bucketId);
            final AlbumQuery.Album album = new AlbumQuery.Album(bucketId, bucketName, count, bucketThumbnail, bucketThumbnailId);
            output.add(album);
            bucketIdArr.add(bucketId);
          }

        } while (cursor.moveToNext());
      }
    }

    return output;
  }

  private static int getCount(@NonNull final Context context, @NonNull final Uri contentUri, @NonNull final String bucketId) {
    try (final Cursor cursor = context.getContentResolver().query(contentUri,
      null, MediaStore.Images.Media.BUCKET_ID + "=?", new String[]{bucketId}, null)) {
      return ((cursor == null) || (cursor.moveToFirst() == false)) ? 0 : cursor.getCount();
    }
  }

  public static final class Album {
    @NonNull
    public final String buckedId;
    @NonNull
    public final String bucketName;
    public final int count;
    public final Uri thumbnail;
    public final Integer thumbnailId;

    Album(@NonNull final String bucketId, @NonNull final String bucketName, final int count, final Uri thumbnail, final Integer thumbnailId) {
      this.buckedId = bucketId;
      this.bucketName = bucketName;
      this.count = count;
      this.thumbnail = thumbnail;
      this.thumbnailId = thumbnailId;
    }
  }
}
