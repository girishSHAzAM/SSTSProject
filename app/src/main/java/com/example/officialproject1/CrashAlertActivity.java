package com.example.officialproject1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class CrashAlertActivity extends AppCompatActivity {
    AlertDialog.Builder builder;
    AlertDialog alert;
    Intent locIntent;
    boolean crashed = false;
    static final int TIME_OUT = 10000;
    static final int MSG_DISMISS_DIALOG = 0;
        private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_DISMISS_DIALOG:
                    if (alert != null && alert.isShowing()) {
                        crashed = true;
                        Intent i = new Intent();
                        i.setAction("crashMessage");
                        locIntent = getIntent();
                        i.putExtra("Crashed",crashed);
                        i.putExtra("locBundle",locIntent.getBundleExtra("locBundle"));
                        sendBroadcast(i);
                        alert.dismiss();
                        finish();
                    }
                    break;

                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_alert);
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Crash Detected");
        builder.setMessage("Press No if Otherwise within 10 seconds !");
        builder.setCancelable(false);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                finish();
            }
        });
        if(!alert.isShowing()) {
            alert = builder.create();
            alert.show();
            mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, TIME_OUT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
