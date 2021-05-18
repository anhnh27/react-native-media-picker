package com.reactnativemediapicker;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;
import com.reactnativemediapicker.fragments.ActionBottomDialogFragment;
import com.reactnativemediapicker.fragments.ImagePickerFragment;
import com.reactnativemediapicker.interfaces.DismissEvent;
import com.reactnativemediapicker.interfaces.FinishedEvent;
import com.reactnativemediapicker.models.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class MediaPickerModule extends ReactContextBaseJavaModule
  implements FinishedEvent, DismissEvent, PermissionListener {

  private static final String PERMISSION_NOT_GRANTED_CODE = "-1";
  private static final String PERMISSION_NOT_GRANTED_CODE_MESSAGE = "STORAGE permission is not granted";
  private static final String USER_CANCEL_CODE = "-2";
  private static final String USER_CANCEL_CODE_MESSAGE = "User cancel";
  private final int STORAGE_REQUEST_CODE = 1001;
  private final int DEFAULT_IMAGE_PICKER_REQUEST_CODE = 1002;
  private final ReactApplicationContext reactContext;
  public int MAX_FILES = 0;
  public boolean TC_SUPPORT = false;
  private int MAX_ALLOWED_BYTES = 0;
  private FragmentManager fragmentManager;
  private Promise iPromise;


  public final ActivityEventListener mActivityResultListener = new BaseActivityEventListener() {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode,
      Intent intent) {
      if (requestCode == DEFAULT_IMAGE_PICKER_REQUEST_CODE) {
        if (resultCode == Activity.RESULT_OK) {
          ArrayList<Uri> selectedImagePaths = new ArrayList<>();
          ClipData mClipData = intent.getClipData();

          if (mClipData != null) {
            for (int i = 0; i < mClipData.getItemCount(); i++) {
              ClipData.Item item = mClipData.getItemAt(i);
              Uri uri = item.getUri();
              selectedImagePaths.add(uri);
            }
          } else {
            Uri uri = intent.getData();
            selectedImagePaths.add(uri);
          }
          ProcessImageTask asyncTask = new ProcessImageTask();
          asyncTask.execute(selectedImagePaths);
        } else {
          onDismiss();
        }
      }
    }
  };

  public MediaPickerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.reactContext.addActivityEventListener(mActivityResultListener);
  }

  @NonNull
  @Override
  public String getName() {
    return "MediaPicker";
  }

  private int extractInt(String name, ReadableMap map) {
    try {
      return map.getInt(name);
    } catch (Exception ex) {
      return 0;
    }
  }

  @ReactMethod
  public void openGallery(final ReadableMap params, final Promise promise) {

    MAX_ALLOWED_BYTES = extractInt("maxAllowedBytes", params);
    MAX_FILES = extractInt("maxFiles", params);
    TC_SUPPORT = params.getBoolean("tcSupport");
    if (!TC_SUPPORT) {
      setLocale(reactContext.getCurrentActivity(), "en");
    }

    iPromise = promise;
    if (ContextCompat.checkSelfPermission(reactContext,
      Manifest.permission.READ_EXTERNAL_STORAGE)
      == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(reactContext,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
        == PackageManager.PERMISSION_GRANTED) {
      showOptions();
    } else {
      PermissionAwareActivity activity = (PermissionAwareActivity) getCurrentActivity();
      activity.requestPermissions(
        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
        STORAGE_REQUEST_CODE, this);
    }
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions,
    int[] grantResults) {
    if (requestCode == STORAGE_REQUEST_CODE) {
      if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
        showOptions();
        return true;
      } else {
        iPromise.reject(PERMISSION_NOT_GRANTED_CODE, PERMISSION_NOT_GRANTED_CODE_MESSAGE);
        Log.d(this.getClass().getName(), PERMISSION_NOT_GRANTED_CODE_MESSAGE);
        return false;
      }
    }
    return false;
  }

  private void showOptions() {
    FinishedEvent finishedEvent = this;
    DismissEvent dismissEvent = this;
    ActionBottomDialogFragment addPhotoBottomDialogFragment = ActionBottomDialogFragment
      .newInstance();
    addPhotoBottomDialogFragment.dismissEvent = dismissEvent;
    addPhotoBottomDialogFragment.onOtherAppsSelected = v -> {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
      getCurrentActivity().startActivityForResult(Intent.createChooser(intent, "Select Picture"),
        DEFAULT_IMAGE_PICKER_REQUEST_CODE);
      addPhotoBottomDialogFragment.dismiss();
    };
    addPhotoBottomDialogFragment.onMyAlbumsSelected = v -> {
      ImagePickerFragment pickerFragment = new ImagePickerFragment(
        reactContext.getCurrentActivity());
      pickerFragment.finishedEvent = finishedEvent;
      pickerFragment.dismissEvent = dismissEvent;
      pickerFragment.maxPhotos = MAX_FILES;
      pickerFragment.setRetainInstance(false);
      fragmentManager = ((FragmentActivity) reactContext.getCurrentActivity())
        .getSupportFragmentManager();
      fragmentManager.beginTransaction()
        .add(pickerFragment, "ImagePickerFragment")
        .commit();
      addPhotoBottomDialogFragment.dismiss();
    };
    addPhotoBottomDialogFragment
      .show(((AppCompatActivity) getCurrentActivity()).getSupportFragmentManager(),
        ActionBottomDialogFragment.TAG);
  }

  @Override
  public void onDismiss() {
    iPromise.reject(USER_CANCEL_CODE, USER_CANCEL_CODE_MESSAGE);
    View photoPickerView = reactContext.getCurrentActivity().findViewById(R.id.photoPickerView);
    if (photoPickerView != null) {
      ((ViewGroup) photoPickerView.getParent()).removeView(photoPickerView);
    }

    View pickerContainer = reactContext.getCurrentActivity().findViewById(R.id.pickerContainer);
    if (pickerContainer != null) {
      ((ViewGroup) pickerContainer.getParent()).removeView(pickerContainer);
    }
  }

  @Override
  public void onFinished(ArrayList<Uri> uriList) {
    cleanCacheFolder();
    ProcessImageTask asyncTask = new ProcessImageTask();
    asyncTask.execute(uriList);
  }

  private Bitmap getBitmap(Context context, Uri imageUri) throws IOException {
    Uri uri;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      uri = imageUri;
    } else {
      // Check for content scheme, usage for `Others App` selection in OS version <= Android O
      uri =
        imageUri.getScheme() != null && imageUri.getScheme().equalsIgnoreCase("content") ? imageUri
                                                                                         : Uri
          .fromFile(new File(imageUri.getPath()));
    }
    InputStream imageStream = context.getContentResolver().openInputStream(uri);
    Bitmap bitmap = null;
    if (imageStream != null) {
      bitmap = BitmapFactory.decodeStream(imageStream);
      imageStream.close();
    }
    return rotateImage(bitmap, uri, context);
  }

  private Bitmap rotateImage(Bitmap bitmap, Uri uri, Context context) throws IOException {
    int rotate = 0;
    InputStream imageStream = context.getContentResolver().openInputStream(uri);
    ExifInterface exif;
    exif = new ExifInterface(imageStream);
    imageStream.close();
    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
      ExifInterface.ORIENTATION_NORMAL);
    switch (orientation) {
    case ExifInterface.ORIENTATION_ROTATE_270:
      rotate = 270;
      break;
    case ExifInterface.ORIENTATION_ROTATE_180:
      rotate = 180;
      break;
    case ExifInterface.ORIENTATION_ROTATE_90:
      rotate = 90;
      break;
    }
    Matrix matrix = new Matrix();
    matrix.postRotate(rotate);
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
  }

  private String bitmapToFile(Bitmap bitmap, @NonNull String fileName, int newWidth,
    int newHeight) throws IOException {
    String tempFolderPath = reactContext.getCacheDir().getPath();
    File newImage = new File(tempFolderPath, fileName);
    newImage.deleteOnExit();
    try {
      FileOutputStream stream = new FileOutputStream(newImage);
      bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
      stream.flush();
      stream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return newImage.getPath();
  }

  private void cleanCacheFolder() {
    String tempFolderPath = reactContext.getCacheDir().getPath();
    File[] files = new File(tempFolderPath).listFiles();
    for (File file : files) {
      if (file.isFile()) {
        file.delete();
      }
    }
    Log.i("ImgPicker", "Temporary files deleted!!!");
  }

  private void setLocale(Activity activity, String languageCode) {
    Locale locale = new Locale(languageCode);
    Locale.setDefault(locale);
    Resources resources = activity.getResources();
    Configuration config = resources.getConfiguration();
    config.setLocale(locale);
    resources.updateConfiguration(config, resources.getDisplayMetrics());
  }

  private class ProcessImageTask extends AsyncTask<ArrayList<Uri>, Void, ArrayList<Response>> {
    ProgressDialog mDialog = new ProgressDialog(reactContext.getCurrentActivity());

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      mDialog.setMessage("Please wait...");
      mDialog.setCancelable(false);
      mDialog.show();
    }

    @SafeVarargs
    @Override
    protected final ArrayList<Response> doInBackground(ArrayList<Uri>... params) {
      ArrayList<Uri> uriList = params[0];
      ArrayList<Response> results = new ArrayList<>();
      for (Uri uri : uriList) {
        try {
          Bitmap original = getBitmap(reactContext, uri);
          long currentBytes = original.getByteCount(); //w * h * 4
          float ratio = 1;
          if (MAX_ALLOWED_BYTES != 0) {
            ratio = (float) Math.sqrt((float) MAX_ALLOWED_BYTES / (float) currentBytes);
          }
          if (ratio > 1) {
            ratio = 1;
          }
          float originalRatio = (float) original.getHeight() / (float) original.getWidth();
          int newWidth = (int) (original.getWidth() * ratio);
          int newHeight = (int) (newWidth * originalRatio);
          Bitmap resizedImg = Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
          String imageName = UUID.randomUUID() + ".jpg";
          String newImgPath = bitmapToFile(resizedImg, imageName, newWidth, newHeight);
          results.add(new Response(newImgPath, resizedImg.getByteCount(), newWidth, newHeight));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      return results;
    }

    @Override
    protected void onPostExecute(ArrayList<Response> response) {
      super.onPostExecute(response);
      mDialog.hide();
      {
        WritableArray promiseArray = new WritableNativeArray();
        for (Response item : response) {
          WritableMap imgMap = new WritableNativeMap();
          imgMap.putString("path", item.getPath());
          imgMap.putInt("size", item.getSize());
          imgMap.putInt("width", item.getWidth());
          imgMap.putInt("height", item.getHeight());
          promiseArray.pushMap(imgMap);
        }

        View photoPickerView = reactContext.getCurrentActivity().findViewById(R.id.photoPickerView);
        if (photoPickerView != null) {
          photoPickerView.animate()
            .translationY(0)
            .alpha(0.0f)
            .setListener(new AnimatorListenerAdapter() {
              @Override
              public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ((ViewGroup) photoPickerView.getParent()).removeView(photoPickerView);
              }
            });
        }

        View pickerContainer = reactContext.getCurrentActivity().findViewById(R.id.pickerContainer);
        if (pickerContainer != null) {
          pickerContainer.animate()
            .translationY(0)
            .alpha(0.0f)
            .setListener(new AnimatorListenerAdapter() {
              @Override
              public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ((ViewGroup) pickerContainer.getParent()).removeView(pickerContainer);
              }
            });
        }

        iPromise.resolve(promiseArray);
      }
    }
  }
}
