package com.reactnativemediapicker.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.reactnativemediapicker.R;
import com.reactnativemediapicker.interfaces.DismissEvent;

import org.jetbrains.annotations.NotNull;

public class ActionBottomDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

  public static final String TAG = "ActionBottomDialog";

  public static ActionBottomDialogFragment newInstance() {
    return new ActionBottomDialogFragment();
  }

  public DismissEvent dismissEvent;

  public @Nullable
  View.OnClickListener onOtherAppsSelected;
  public @Nullable
  View.OnClickListener onMyAlbumsSelected;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.bottom_sheet, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    view.findViewById(R.id.option2).setOnClickListener(onOtherAppsSelected);
    view.findViewById(R.id.option1).setOnClickListener(onMyAlbumsSelected);
  }

  @Override
  public void onAttach(@NotNull Context context) {
    super.onAttach(context);
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  @Override
  public void onClick(View view) {
    dismiss();
  }

  @Override
  public void onCancel(@NonNull DialogInterface dialog) {
    dismissEvent.onDismiss();
    super.onCancel(dialog);
  }
}
