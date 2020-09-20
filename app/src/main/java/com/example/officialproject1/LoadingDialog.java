package com.example.officialproject1;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoadingDialog {
   private Activity activity;
   private AlertDialog loadDialog;
    LoadingDialog(Activity myActivity){
        activity = myActivity;
    }
    void startLoadDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog,null));
        loadDialog = builder.create();
        loadDialog.setCancelable(false);
        loadDialog.setCanceledOnTouchOutside(false);
        loadDialog.show();
    }
    void dismissLoadDialog(){
        loadDialog.dismiss();
    }
}
