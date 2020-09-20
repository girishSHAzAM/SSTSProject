package com.example.officialproject1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class login_activity extends AppCompatActivity {
String emailid,password;
EditText emailInput,passwordInput;
LoadingDialog loader;
Button loginButton;
TextView linkSignup;
FirebaseAuth auth;
FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loader = new LoadingDialog(login_activity.this);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(login_activity.this,DisplayActivity.class);
            startActivity(intent);
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = FirebaseFirestore.getInstance();
        emailInput = findViewById(R.id.input_email_login);
        passwordInput = findViewById(R.id.input_password_login);
        loginButton = findViewById(R.id.btn_login);
        linkSignup = findViewById(R.id.link_signup);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailid = emailInput.getText().toString().toLowerCase();
                password = passwordInput.getText().toString();
                if(emailid.isEmpty()){
                    Toast.makeText(login_activity.this,"Please enter your email address",Toast.LENGTH_SHORT).show();
                }
                else if(password.isEmpty()){
                    Toast.makeText(login_activity.this,"Please enter your password",Toast.LENGTH_SHORT).show();
                }
                else{
                    loader.startLoadDialog();
                    auth.signInWithEmailAndPassword(emailid,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> taskAuth) {
                            if (taskAuth.isSuccessful()) {
                                if (auth.getCurrentUser().isEmailVerified()) {
                                    Intent intentSuccess = new Intent(login_activity.this,DisplayActivity.class);
                                    Toast.makeText(login_activity.this,"Login successful!",Toast.LENGTH_SHORT).show();
                                    loader.dismissLoadDialog();
                                    startActivity(intentSuccess);
                                    finish();
                                }
                                else{
                                    loader.dismissLoadDialog();
                                    Toast.makeText(login_activity.this,"Please verify your email address to sign in",Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                loader.dismissLoadDialog();
                                Toast.makeText(login_activity.this, "Login Unsuccessful. Check Email ID and Password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
    public void moveToRegisterActivity(View v){
        Intent intent = new Intent(login_activity.this,register_Activity.class);
        startActivity(intent);
    }
    public void moveToForgotActivity(View v){
        Intent intent = new Intent(login_activity.this,ForgotPassActivity.class);
        startActivity(intent);
    }

}

