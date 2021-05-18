package com.reactnativemediapicker.interfaces;

import android.net.Uri;
import java.util.ArrayList;

public interface FinishedEvent {
  void onFinished(ArrayList<Uri> uriList);
}
