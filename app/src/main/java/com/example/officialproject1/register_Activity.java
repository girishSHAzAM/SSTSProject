package com.example.officialproject1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.FirebaseFirestore;



import java.util.HashMap;
import java.util.Map;

public class register_Activity extends AppCompatActivity {
String userName,userAddress,userEmail,userMobile,userPassword,reEnter,eNo;
EditText name,address,email,mobile,password,reEnterPassword,eNumber;
Button signup;
FirebaseAuth mFirebaseAuth;
TextView linkLogin;
FirebaseFirestore db;
Thread dbStoreThread;
Handler dbStoreHandler;
LoadingDialog loader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        name = findViewById(R.id.input_name);
        address = findViewById(R.id.input_address);
        email = findViewById(R.id.input_email_signup);
        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mobile = findViewById(R.id.input_mobile);
        password = findViewById(R.id.input_password);
        reEnterPassword = findViewById(R.id.input_reEnterPassword);
        eNumber = findViewById(R.id.input_emergency);
        signup = findViewById(R.id.btn_signup);
        linkLogin = findViewById(R.id.link_login);
        loader = new LoadingDialog(register_Activity.this);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loader.startLoadDialog();
                userName = name.getText().toString();
                userAddress = address.getText().toString();
                userEmail = email.getText().toString();
                userMobile = mobile.getText().toString();
                userPassword = password.getText().toString();
                reEnter = reEnterPassword.getText().toString();
                eNo = eNumber.getText().toString();
                boolean emptyCheck = checkEmpty(userName,userAddress,userEmail,userMobile,userPassword,reEnter,eNo);
                if(emptyCheck != true){
                    if(userPassword.length() > 6){
                        if(userPassword.equals(reEnter)){
                            mFirebaseAuth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful() != true){
                                        loader.dismissLoadDialog();
                                        String error = task.getException().toString();
                                        Toast.makeText(register_Activity.this,"Registration Unsuccessful "+error,Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        mFirebaseAuth.getCurrentUser().sendEmailVerification();
                                        dbStoreHandler = new Handler(){
                                            @Override
                                            public void handleMessage(Message msg) {
                                                if (msg.getData().getString("Result").equals("1")) {
                                                    loader.dismissLoadDialog();
                                                    Toast.makeText(register_Activity.this, "Registration Successful. Please verify your email before you login.", Toast.LENGTH_SHORT).show();
                                                    onBackPressed();
                                                }
                                                else{
                                                    loader.dismissLoadDialog();
                                                    Toast.makeText(register_Activity.this,"Registration Unsuccessful"+msg.getData().getString("Result"),Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        };
                                        dbStoreThread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Map<String, String> users = new HashMap<>();
                                                users.put("Name", userName);
                                                users.put("Address", userAddress);
                                                users.put("Email", userEmail);
                                                users.put("Mobile",userMobile);
                                                users.put("eContact",eNo);
                                                final Bundle bundle = new Bundle();
                                                        db.collection("users")
                                                        .document(userEmail.toLowerCase())
                                                        .set(users).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Message msg = dbStoreHandler.obtainMessage();
                                                        bundle.putString("Result","1");
                                                        msg.setData(bundle);
                                                        dbStoreHandler.sendMessage(msg);
                                                    }
                                                })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Message msg = dbStoreHandler.obtainMessage();
                                                                bundle.putString("Result",e.getMessage());
                                                                msg.setData(bundle);
                                                                dbStoreHandler.sendMessage(msg);
                                                            }
                                                        });
                                            }
                                        });
                                        dbStoreThread.start();
                                    }
                                }
                            });
                        }
                        else{
                            loader.dismissLoadDialog();
                            Toast.makeText(register_Activity.this,"The passwords do not match",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        loader.dismissLoadDialog();
                        Toast.makeText(register_Activity.this,"Password must be at least 7 characters",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    loader.dismissLoadDialog();
                }
            }
        });
    }
    public void moveToLoginActivity(View view){
        onBackPressed();
    }
    public boolean checkEmpty(String name,String address,String email,String mobile,String password,String reEnter,String eNo){
        if(name.isEmpty()){
            Toast.makeText(register_Activity.this,"Name field is empty",Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(address.isEmpty()){
            Toast.makeText(register_Activity.this,"Address field is empty",Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(email.isEmpty()){
            Toast.makeText(register_Activity.this,"Email field is empty",Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(mobile.isEmpty()){
            Toast.makeText(register_Activity.this,"Mobile field is empty",Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(password.isEmpty()){
            Toast.makeText(register_Activity.this,"Password field is empty",Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(reEnter.isEmpty()){
            Toast.makeText(register_Activity.this,"Re-enter password field is empty",Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(eNo.isEmpty()){
            Toast.makeText(register_Activity.this,"Emergency Contact field is empty",Toast.LENGTH_SHORT).show();
            return true;
        }
        else{
            return false;
        }
    }
}
