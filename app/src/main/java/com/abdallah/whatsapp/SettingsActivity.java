package com.abdallah.whatsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userprofileImageView;

    private Toolbar SettingsToolBar;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int GalleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID= mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();




        InitializedFields();

        userName.setVisibility(View.INVISIBLE);

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetriveUserInformation();


 

    }




    private void InitializedFields() {
        UpdateAccountSettings = (Button) findViewById(R.id.update_setting_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_stutes);
        userprofileImageView = (CircleImageView) findViewById(R.id.set_profile_image);

        SettingsToolBar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }







    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please write your user name first...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setUserStatus)){
            Toast.makeText(this, "Please write your Status...", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setUserStatus);

            RootRef.child("Users").child(currentUserID).updateChildren( profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile updated successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void RetriveUserInformation() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){
                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveUseStarus = dataSnapshot.child("status").getValue().toString();
                            String retriveUseProfileImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retriveUserName);
                            userName.setText(retriveUseStarus);

                        }
                        else if( (dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveUseStarus = dataSnapshot.child("status").getValue().toString();

                            userName.setText(retriveUserName);
                            userStatus.setText(retriveUseStarus);

                        }
                        else{
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please set & Update profile information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
