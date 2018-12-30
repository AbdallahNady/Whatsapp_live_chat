package com.abdallah.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountButton ;
    private EditText UserEmail , UserPassword;
    private TextView AlreadyHaveAccoutLink;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ProgressDialog lodingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        InitilizedFields();

        AlreadyHaveAccoutLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendToLoginActiviy();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAcoount();
            }
        });
    }



    private void InitilizedFields() {
        CreateAccountButton = (Button) findViewById(R.id.register_button);

        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);

        AlreadyHaveAccoutLink = (TextView) findViewById(R.id.already_have_account_link_reg);

        lodingbar = new ProgressDialog(this);
    }

    private void SendToLoginActiviy() {
        Intent RegisterIntent = new Intent(RegisterActivity.this , LoginActivity.class);
        startActivity(RegisterIntent);    }

    private void CreateNewAcoount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_LONG).show();
        }
        else {
            lodingbar.setTitle("Creating New Account");
            lodingbar.setMessage("Please Wait, While we ware creating account for you...");
            lodingbar.setCanceledOnTouchOutside(true);
            lodingbar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                String deviceToken = FirebaseInstanceId.getInstance().getToken();


                                String currentUserID = mAuth.getCurrentUser().getUid();
                                RootRef.child("Users").child(currentUserID).setValue("");

                                RootRef.child("Users").child(currentUserID).child("device_token")
                                        .setValue(deviceToken);

                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                lodingbar.dismiss();
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                lodingbar.dismiss();
                            }
                        }
                    });
        }

    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
