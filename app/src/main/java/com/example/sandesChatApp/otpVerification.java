package com.example.sandesChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class otpVerification extends AppCompatActivity {

    TextView mChangeNumber;
    EditText mGetOtp;
    android.widget.Button mSubmit;
    String enteredOtp;

    FirebaseAuth firebaseAuth;
    ProgressBar mProgressBarForAuth;

    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        mChangeNumber = findViewById(R.id.changeNumber);
        mGetOtp = findViewById(R.id.getOTP);
        mSubmit = findViewById(R.id.verify);
        mProgressBarForAuth = findViewById(R.id.progressBarOtpAuth);
        firebaseAuth = FirebaseAuth.getInstance();

        Bundle data = getIntent().getExtras();

        SmsReceiver.bindListener(new SmsListner() {
            @Override
            public void messageReceived(String messageText) {
                mGetOtp.setText(messageText);
            }
        });

        mChangeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(otpVerification.this, MainActivity.class);
                startActivity(i);
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enteredOtp = mGetOtp.getText().toString();
                if(enteredOtp.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter the OTP first", Toast.LENGTH_SHORT).show();
                }
                else {
                    mProgressBarForAuth.setVisibility(View.VISIBLE);
                    String serverOtp = data.getString("otp");
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(serverOtp, enteredOtp);
                    signInWithPhoneAuthCredentials(credential);
                }
            }
        });
    }

    private void signInWithPhoneAuthCredentials(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    mProgressBarForAuth.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Verification done successfully", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(otpVerification.this, setProfile.class);
                    startActivity(i);
                    finish();
                }
                else{
                    mProgressBarForAuth.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Could not verify OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, MY_PERMISSIONS_REQUEST_RECEIVE_SMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == MY_PERMISSIONS_REQUEST_RECEIVE_SMS){
            if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getApplicationContext(),"Permission Granted, Thank you :)", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getApplicationContext(),"Permission Denied !!!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
