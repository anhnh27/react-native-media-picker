package com.reactnativemediapicker.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.reactnativemediapicker.R;
import com.reactnativemediapicker.adapters.AlbumListAdapter;
import com.reactnativemediapicker.adapters.PhotoListAdapter;
import com.reactnativemediapicker.helpers.AlbumQuery;
import com.reactnativemediapicker.interfaces.AlbumClickEvent;
import com.reactnativemediapicker.interfaces.DismissEvent;
import com.reactnativemediapicker.interfaces.FinishedEvent;
import com.reactnativemediapicker.interfaces.PhotoClickEvent;
import com.reactnativemediapicker.models.Album;
import com.reactnativemediapicker.models.Photo;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ImagePickerFragment extends Fragment implements AlbumClickEvent, PhotoClickEvent {

  public static ArrayList<Integer> selectedPhotos;
  private final Activity mActivity;
  public int maxPhotos;
  public FinishedEvent finishedEvent;
  public DismissEvent dismissEvent;
  private RecyclerView recyclerView;
  private ViewGroup photoPickerView;
  private ArrayList<Album> albums;
  private ArrayList<Photo> photos;
  private AlbumListAdapter albumData;
  private PhotoListAdapter photoListAdapter;

  public ImagePickerFragment(Activity activity) {
    mActivity = activity;
  }

  private int getStatusBarHeight() {
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Glide.get(mActivity).setMemoryCategory(MemoryCategory.HIGH);
    albums = fetchAlbums();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
    selectedPhotos = new ArrayList<>();
    ViewGroup pickerView = (ViewGroup) inflater
      .inflate(R.layout.image_picker_container, container, false);
    pickerView.setBackgroundColor(Color.WHITE);
    recyclerView = setupRecycleView(pickerView);
    mActivity.addContentView(pickerView,
      new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));

    Toolbar toolbar = pickerView.findViewById(R.id.toolbar);
    ((AppCompatActivity) mActivity).setSupportActionBar(toolbar);
    ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayShowTitleEnabled(false);
    ImageView closeBtn = toolbar.findViewById(R.id.toolbar_close);
    closeBtn.setOnClickListener(v -> dismissEvent.onDismiss());

    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) pickerView.getLayoutParams();
    params.setMargins(0, getStatusBarHeight(), 0, 0);
    pickerView.setLayoutParams(params);

    pickerView.setFocusableInTouchMode(true);
    pickerView.requestFocus();
    pickerView.setOnKeyListener((v, keyCode, e) -> {
      if (keyCode == KeyEvent.KEYCODE_BACK) {
        dismissEvent.onDismiss();
        return true;
      } else {
        return false;
      }
    });

    return pickerView;
  }

  private ArrayList<Uri> getImageUri(ArrayList<Integer> source) {
    ArrayList<Uri> results = new ArrayList<>();
    for (int pos : source) {
      Uri uri;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        uri = ContentUris
          .withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, photos.get(pos).getId());
      } else {
        uri = photos.get(pos).getUri();
      }
      results.add(uri);
    }
    return results;
  }

  private RecyclerView setupRecycleView(View parent) {
    RequestManager requestManager = Glide.with(this);
    albumData = new AlbumListAdapter(albums, requestManager, getResources());
    albumData.event = this;
    GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 2,
      LinearLayoutManager.VERTICAL, false);
    recyclerView = parent.findViewById(R.id.recyclerview);
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(gridLayoutManager);
    recyclerView.setAdapter(albumData);
    return recyclerView;
  }

  private ArrayList<Album> fetchAlbums() {
    ArrayList<Album> albums = new ArrayList<>();
    ArrayList<AlbumQuery.Album> albumsQR = AlbumQuery.get(mActivity);
    for (AlbumQuery.Album album : albumsQR) {
      albums.add(new Album(Integer.parseInt(album.buckedId), album.bucketName, album.thumbnailId,
        album.thumbnail, album.count));
    }
    return albums;
  }

  private ArrayList<Photo> fetchPhotoInAlbum(int albumId) {

    ArrayList<Photo> photos = new ArrayList<>();
    Uri queryUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    final String[] projection = {
      MediaStore.Images.ImageColumns._ID,
      MediaStore.Images.ImageColumns.DATA,
    };
    final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
    final String[] selectionArgs = {String.valueOf(albumId)};

    String orderBy = String.format("%s DESC", MediaStore.Images.ImageColumns.DATE_ADDED);

    int id;
    Uri uri;

    Cursor cursor = mActivity.getContentResolver()
      .query(queryUri, projection, selection, selectionArgs, orderBy);
    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID);
    int uriColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
    if (cursor.moveToFirst()) {
      do {
        id = cursor.getInt(idColumn);
        uri = Uri.parse(cursor.getString(uriColumn));
        photos.add(new Photo(id, uri));
      } while (cursor.moveToNext());
    }
    cursor.close();

    return photos;
  }

  @SuppressLint("StringFormatMatches")
  @Override
  public void onAlbumClick(int albumIndex) {
    photos = fetchPhotoInAlbum(albums.get(albumIndex).getId());
    GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
    LayoutInflater inflater = (LayoutInflater) getContext()
      .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    photoPickerView = (ViewGroup) inflater.inflate(R.layout.photo_picker, null);

    LinearLayout photoPickerContainer = photoPickerView.findViewById(R.id.photoPickerContainer);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.MATCH_PARENT
    );

    params.setMargins(0, getStatusBarHeight(), 0, 0);
    photoPickerContainer.setLayoutParams(params);

    RecyclerView recyclerPhotoView = photoPickerView.findViewById(R.id.photoRecyclerView);
    recyclerPhotoView.setHasFixedSize(true);
    recyclerPhotoView.setLayoutManager(layoutManager);

    TextView albumTitle = photoPickerView.findViewById(R.id.albumTitle);
    albumTitle.setText(albums.get(albumIndex).getTitle());

    RequestManager requestManager = Glide.with(this);
    photoListAdapter = new PhotoListAdapter(requestManager, photos);
    photoListAdapter.event = this;
    photoListAdapter.setHasStableIds(true);

    RecyclerViewPreloader<Photo> preloader = new RecyclerViewPreloader<>(requestManager,
      photoListAdapter, photoListAdapter, 36);
    recyclerPhotoView.addOnScrollListener(preloader);
    recyclerPhotoView.setAdapter(photoListAdapter);

    mActivity.addContentView(photoPickerView,
      new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));

    Button btnClosePhoto = photoPickerView.findViewById(R.id.btnClosePhoto);
    btnClosePhoto.setOnClickListener(v -> {
      dismissEvent.onDismiss();
    });

    Button btnBack = photoPickerView.findViewById(R.id.btnBack);
    btnBack.setOnClickListener(v -> {
      View photoPickerView = mActivity.findViewById(R.id.photoPickerView);
      if (photoPickerView != null) {
        selectedPhotos = new ArrayList<>();
        photoListAdapter.notifyDataSetChanged();
        updateSelectedPhotosText();
        ((ViewGroup) photoPickerView.getParent()).removeView(photoPickerView);
      }
    });

    Button btnClear = photoPickerView.findViewById(R.id.btnClear);
    btnClear.setOnClickListener(v -> {
      selectedPhotos = new ArrayList<>();
      photoListAdapter.notifyDataSetChanged();
      updateSelectedPhotosText();
    });

    Button btnDone = photoPickerView.findViewById(R.id.btnDone);
    btnDone.setOnClickListener(v -> {
      finishedEvent.onFinished(getImageUri(selectedPhotos));
    });

    TextView selectedPhotoText = photoPickerView.findViewById(R.id.selectedPhotoTxt);
    selectedPhotoText.setText(String.format(getResources().getString(R.string.number_of_photos_selected), selectedPhotos.toArray().length));

  }

  @Override
  public void onPhotoClick(int position) {
    if (selectedPhotos.size() > maxPhotos - 1 && maxPhotos != 0 && !selectedPhotos
      .contains(position)) {
      Toast
        .makeText(mActivity, "Cannot select more than " + maxPhotos + " photos!", Toast.LENGTH_LONG)
        .show();
    } else {
      int selectedIndex = selectedPhotos.indexOf(position);
      if (selectedIndex != -1) {
        selectedPhotos.remove(selectedIndex);
      } else {
        selectedPhotos.add(position);
      }
      updateSelectedPhotosText();
    }
  }

  @SuppressLint("StringFormatMatches")
  private void updateSelectedPhotosText() {
    Resources res = getResources();
    TextView selectedPhotoText = photoPickerView.findViewById(R.id.selectedPhotoTxt);
    selectedPhotoText.setText(String.format(res.getString(R.string.number_of_photos_selected), selectedPhotos.toArray().length));
  }

  @Override
  public void onDetach() {
    super.onDetach();
    if ((albumData != null || photoListAdapter != null) && recyclerView != null) {
      recyclerView.setAdapter(null);
      recyclerView = null;
    }
    selectedPhotos = null;
  }
}

