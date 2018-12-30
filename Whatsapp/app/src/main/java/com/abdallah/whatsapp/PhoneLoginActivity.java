package com.abdallah.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerfiyButton, VerfiyButton;
    private EditText InputPhoneNumber, InputVerficationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;


    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        mAuth = FirebaseAuth.getInstance();


        sendVerfiyButton = (Button) findViewById(R.id.send_ver_code_buttons);
        VerfiyButton = (Button) findViewById(R.id.verifiy_buttons);

        InputPhoneNumber = (EditText) findViewById(R.id.phone_numper_input);
        InputVerficationCode = (EditText) findViewById(R.id.verfication_code_input);

        loadingBar = new ProgressDialog(this);


        sendVerfiyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = InputPhoneNumber.getText().toString();
                
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "Please enter, Your Phone number first ", Toast.LENGTH_SHORT).show();
                }
                else{
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait, While we are authentication your phone...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }

            }
        });

        VerfiyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendVerfiyButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String verficationCode = InputVerficationCode.getText().toString();
                if (TextUtils.isEmpty(verficationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Please write Verification code", Toast.LENGTH_SHORT).show();
                }
                else{

                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please wait, While we are verifiying verification code...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verficationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invaild Phone Number, Please enter correct phone number with your country code", Toast.LENGTH_LONG).show();

                sendVerfiyButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                VerfiyButton.setVisibility(View.INVISIBLE);
                InputVerficationCode.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent, please checked and verify...", Toast.LENGTH_LONG).show();

                sendVerfiyButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                VerfiyButton.setVisibility(View.VISIBLE);
                InputVerficationCode.setVisibility(View.VISIBLE);
            }

        };
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Logged successfully...", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        } else {
                            String message  = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
