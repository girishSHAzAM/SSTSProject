package com.example.officialproject1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

public class UpdateEmailActivity extends AppCompatActivity {
TextView oldEmailText,newEmailText,passText;
Button updateEmail;
EditText oldEmail,newEmail,password;
FirebaseAuth auth;
FirebaseUser user;
FirebaseFirestore db;
LoadingDialog loadingDialog;
Thread dbFetch,dbStore;
Handler dbFetchHandler;
String name,email,mobile,address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);
        loadingDialog = new LoadingDialog(UpdateEmailActivity.this);
        oldEmailText = findViewById(R.id.email_desc);
        newEmailText = findViewById(R.id.newEmail_desc);
        passText = findViewById(R.id.passwd_desc);
        oldEmail = findViewById(R.id.update_email);
        newEmail = findViewById(R.id.new_email);
        password = findViewById(R.id.passwdFrEmail);
        updateEmail = findViewById(R.id.btn_updateEmail);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        updateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.startLoadDialog();
                dbFetchHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        Bundle bundle = msg.getData();
                        name = bundle.getString("Name");
                        email = bundle.getString("Email");
                        mobile = bundle.getString("Mobile");
                        address = bundle.getString("Address");
                    }
                };
                dbFetch = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        db.collection("users").document(user.getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot document = task.getResult();
                                Message oldData = dbFetchHandler.obtainMessage();
                                Bundle oldValues = new Bundle();
                                oldValues.putString("Name",document.getString("Name"));
                                oldValues.putString("Mobile",document.getString("Mobile"));
                                oldValues.putString("Email",document.getString("Email"));
                                oldValues.putString("Address",document.getString("Address"));
                                oldData.setData(oldValues);
                                dbFetchHandler.sendMessage(oldData);
                            }
                        });
                    }
                });
                dbFetch.start();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(oldEmail.getText().toString(), password.getText().toString());
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updateEmail(newEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        user.sendEmailVerification();
                                        dbStore = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Map<String,String> theMap = new HashMap<>();
                                                theMap.put("Name",name);
                                                theMap.put("Email",newEmail.getText().toString());
                                                theMap.put("Mobile",mobile);
                                                theMap.put("OldEmail",email.toLowerCase());
                                                theMap.put("Address",address);
                                                db.collection("users").document(newEmail.getText().toString().toLowerCase()).set(theMap);
                                            }
                                        });
                                        dbStore.start();
                                        auth.signOut();
                                        Intent intent = new Intent(UpdateEmailActivity.this, login_activity.class);
                                        loadingDialog.dismissLoadDialog();
                                        Toast.makeText(UpdateEmailActivity.this, "Email has been updated!", Toast.LENGTH_SHORT).show();
                                        startActivity(intent);
                                        finish();
                                    }
                                    else{
                                        loadingDialog.dismissLoadDialog();
                                        Toast.makeText(UpdateEmailActivity.this,"Cannot update email.Please try again later.",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else{
                            Intent intent = new Intent(UpdateEmailActivity.this, ProfileActivity.class);
                            loadingDialog.dismissLoadDialog();
                            Toast.makeText(UpdateEmailActivity.this,"Email/Password is incorrect!",Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });
    }
}
