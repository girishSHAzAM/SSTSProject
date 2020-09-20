package com.example.officialproject1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassActivity extends AppCompatActivity {
Button resetButton;
TextView reset,desc;
EditText resetEmail;
LoadingDialog loadingDialog;
String userEmail;
FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);
        resetButton = findViewById(R.id.btn_reset);
        resetEmail = findViewById(R.id.input_email_reset);
        reset = findViewById(R.id.link_forgot);
        desc = findViewById(R.id.link_desc);
        loadingDialog = new LoadingDialog(ForgotPassActivity.this);
        auth = FirebaseAuth.getInstance();
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.startLoadDialog();
                userEmail = resetEmail.getText().toString();
                auth.sendPasswordResetEmail(userEmail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ForgotPassActivity.this,"Link sent to registered Email",Toast.LENGTH_SHORT).show();
                                    loadingDialog.dismissLoadDialog();
                                    onBackPressed();
                                }
                                else{
                                    Log.d("AnyError",task.getException().toString());
                                    loadingDialog.dismissLoadDialog();
                                }
                            }
                        });
            }
        });
    }
}
