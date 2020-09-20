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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
public class EditActivity extends AppCompatActivity {
String email,name,mobile,address,eContact;
FirebaseAuth auth;
FirebaseFirestore db;
LoadingDialog loadingDialog;
Button updateProfile;
Thread dbFetch,dbStore;
Handler dbFetchHandler,dbStoreHandler;
TextView nameDesc,addressDesc,mobileDesc,eContactDesc;
EditText nameEdit,addressEdit,mobileEdit,eContactEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        loadingDialog = new LoadingDialog(EditActivity.this);
        loadingDialog.startLoadDialog();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        nameDesc = findViewById(R.id.name_desc);
        addressDesc = findViewById(R.id.address_desc);
        mobileDesc = findViewById(R.id.mobile_desc);
        eContactDesc = findViewById(R.id.eContact_desc);
        nameEdit = findViewById(R.id.input_name_edit);
        addressEdit = findViewById(R.id.input_address_edit);
        mobileEdit = findViewById(R.id.input_mobile_edit);
        eContactEdit = findViewById(R.id.input_eContact_edit);
        dbFetchHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                if (bundle.getString("Result").equals("1")) {
                    nameEdit.setText(bundle.getString("Name"));
                    addressEdit.setText(bundle.getString("Address"));
                    mobileEdit.setText(bundle.getString("Mobile"));
                    email = bundle.getString("Email");
                    eContactEdit.setText(bundle.getString("eContact"));
                    loadingDialog.dismissLoadDialog();
                }
                else{
                    Toast.makeText(EditActivity.this,"Error loading data. Please try again.",Toast.LENGTH_SHORT).show();
                    loadingDialog.dismissLoadDialog();
                    onBackPressed();
                }
            }
        };
        dbFetch = new Thread(new Runnable() {
            @Override
            public void run() {
                final Bundle bundle = new Bundle();
                db.collection("users").document(auth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Message msg = dbFetchHandler.obtainMessage();
                            DocumentSnapshot document = task.getResult();
                            bundle.putString("Result","1");
                            bundle.putString("Name",document.getString("Name"));
                            bundle.putString("Address",document.getString("Address"));
                            bundle.putString("Email",document.getString("Email"));
                            bundle.putString("Mobile",document.getString("Mobile"));
                            bundle.putString("eContact",document.getString("eContact"));
                            msg.setData(bundle);
                            dbFetchHandler.sendMessage(msg);
                        }
                        else{
                            Message msg = dbFetchHandler.obtainMessage();
                            bundle.putString("Result","2");
                            msg.setData(bundle);
                            dbFetchHandler.sendMessage(msg);
                        }
                    }
                });
            }
        });
        dbFetch.start();
        updateProfile = findViewById(R.id.btn_updateProfile);
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.startLoadDialog();
                dbStoreHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        Bundle bundle = msg.getData();
                        if(bundle.getString("Result").equals("1")){
                            Toast.makeText(EditActivity.this,"Profile Updated!",Toast.LENGTH_SHORT).show();
                            loadingDialog.dismissLoadDialog();
                            Intent gotoProf = new Intent(EditActivity.this,DisplayActivity.class);
                            startActivity(gotoProf);
                            finish();
                        }else{
                            Toast.makeText(EditActivity.this,"Error updating profile. Please try again.",Toast.LENGTH_SHORT).show();
                            loadingDialog.dismissLoadDialog();
                        }
                    }
                };
                name = nameEdit.getText().toString();
                address = addressEdit.getText().toString();
                mobile = mobileEdit.getText().toString();
                eContact = eContactEdit.getText().toString();
                dbStore = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Map < String, String > updatedUsers = new HashMap<>();
                        updatedUsers.put("Name",name);
                        updatedUsers.put("Address",address);
                        updatedUsers.put("Mobile",mobile);
                        updatedUsers.put("Email",email);
                        updatedUsers.put("eContact",eContact);
                        final Bundle bundle = new Bundle();
                        final Message msg = dbStoreHandler.obtainMessage();
                        db.collection("users").document(auth.getCurrentUser().getEmail()).set(updatedUsers).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                bundle.putString("Result","1");
                                msg.setData(bundle);
                                dbStoreHandler.sendMessage(msg);
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        bundle.putString("Result","2");
                                        msg.setData(bundle);
                                        dbStoreHandler.sendMessage(msg);
                                    }
                                });
                    }
                });
                dbStore.start();
            }
        });
    }
}
