package com.example.officialproject1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class UpdatePasswordActivity extends AppCompatActivity {
    LoadingDialog loadingDialog;
    TextView oldPass,newPass;
    Button updatePassword;
    EditText oldPassword,newPassword;
    FirebaseAuth auth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        loadingDialog = new LoadingDialog(UpdatePasswordActivity.this);
        oldPass = findViewById(R.id.password_desc);
        newPass = findViewById(R.id.newpd_desc);
        oldPassword = findViewById(R.id.update_password);
        newPassword = findViewById(R.id.new_password);
        updatePassword = findViewById(R.id.btn_updatePassword);
        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.startLoadDialog();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(auth.getCurrentUser().getEmail(), oldPassword.getText().toString());
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    user.updatePassword(newPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Intent intent = new Intent(UpdatePasswordActivity.this,ProfileActivity.class);
                                                loadingDialog.dismissLoadDialog();
                                                Toast.makeText(UpdatePasswordActivity.this,"Password update successful.",Toast.LENGTH_SHORT).show();
                                                startActivity(intent);
                                                finish();
                                            }
                                            else{
                                                loadingDialog.dismissLoadDialog();
                                                Toast.makeText(UpdatePasswordActivity.this,"Password cannot be updated.Please try again later.",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                                else{
                                    Toast.makeText(UpdatePasswordActivity.this,"Old password is invalid!",Toast.LENGTH_SHORT).show();
                                    loadingDialog.dismissLoadDialog();
                                    onBackPressed();
                                }
                            }
                        });
            }
        });
    }
}
