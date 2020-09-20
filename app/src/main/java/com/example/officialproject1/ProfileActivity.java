package com.example.officialproject1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
FirebaseAuth auth;
LoadingDialog loadingDialog;
String emailAdd,username,mobile,address,email;
FirebaseFirestore db;
Button editButton;
TextView userProf,nameDesc,nameAns,AddDesc,AddAns,emailDesc,emailAns,mobDesc,mobAns;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        loadingDialog = new LoadingDialog(ProfileActivity.this);
        loadingDialog.startLoadDialog();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        editButton = findViewById(R.id.btn_editProfile);
        userProf = findViewById(R.id.userProfile);
        nameDesc = findViewById(R.id.name_desc);
        nameAns = findViewById(R.id.name_ans);
        AddDesc = findViewById(R.id.address_desc);
        AddAns = findViewById(R.id.address_ans);
        emailDesc = findViewById(R.id.email_desc);
        emailAns = findViewById(R.id.email_ans);
        mobDesc = findViewById(R.id.mob_desc);
        mobAns = findViewById(R.id.mob_ans);
        emailAdd = auth.getCurrentUser().getEmail();
        db.collection("users").document(emailAdd).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    username = document.getString("Name");
                    mobile = document.getString("Mobile");
                    address = document.getString("Address");
                    email = document.getString("Email");
                    nameAns.setText(username);
                    AddAns.setText(address);
                    mobAns.setText(mobile);
                    emailAns.setText(email);
                    loadingDialog.dismissLoadDialog();
                    Toast.makeText(ProfileActivity.this, "Profile loaded", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(ProfileActivity.this,"Error loading data. Please try again.",Toast.LENGTH_SHORT).show();
                    loadingDialog.dismissLoadDialog();
                }
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,EditActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profile_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.change_password:
                Intent intent = new Intent(ProfileActivity.this,UpdatePasswordActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.update_email:
                Intent emailIntent = new Intent(ProfileActivity.this,UpdateEmailActivity.class);
                startActivity(emailIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
