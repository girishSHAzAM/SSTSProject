package com.example.officialproject1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

public class FinishActivity extends AppCompatActivity {
    Intent receiveIntent;
    Bundle receiveData;
    boolean abruptEnd,crashed;
    double score;
TextView performance;
RatingBar rating;
Button finish;
LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        receiveIntent = getIntent();
        receiveData = receiveIntent.getBundleExtra("ResultParameters");
        abruptEnd = receiveData.getBoolean("abruptEnd");
        crashed = receiveData.getBoolean("Crashed");
        score = receiveData.getDouble("Score");
        rating = findViewById(R.id.ratingBar);
        performance = findViewById(R.id.performance);
        loadingDialog = new LoadingDialog(FinishActivity.this);
        loadingDialog.startLoadDialog();
        finish = findViewById(R.id.btn_finish);
        if(crashed == true){
            rating.setVisibility(View.INVISIBLE);
            performance.setText("You have crashed! We have sent a notification to your emergency contact.");
            loadingDialog.dismissLoadDialog();
        }
        else {
            if (abruptEnd == false) {
                rating.setRating((float)score);
                performance.setText("Good Job!");
                loadingDialog.dismissLoadDialog();
            }
            else {
                rating.setVisibility(View.INVISIBLE);
                performance.setText("Oops! Trip ended prematurely.");
                loadingDialog.dismissLoadDialog();
            }
        }
            finish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
}
