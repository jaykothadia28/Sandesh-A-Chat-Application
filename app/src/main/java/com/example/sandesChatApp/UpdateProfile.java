package com.example.sandesChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfile extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton mBackButton;
    private CardView mCardView;
    private ImageView mImageView;
    private EditText mEditText;
    private Button mUpdateButton;
    ProgressDialog progressDialog, progress2;
    private Uri imagePath;
    Intent intent;
    private static final int PICK_IMAGE = 123;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;

    private String imageUriAccessToken, newName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mToolbar = findViewById(R.id.updateProfileToolbar);
        setSupportActionBar(mToolbar);

        mBackButton = findViewById(R.id.backButtonUpdateProfile);
        mImageView = findViewById(R.id.updateUserImageInImageView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Updating Profile !!!");

        mEditText = findViewById(R.id.updateUsername);
        mUpdateButton = findViewById(R.id.updateProfileButton);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        progress2 = new ProgressDialog(this);
        progress2.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress2.setCancelable(false);
        progress2.setTitle("Please wait");
        progress2.setMessage("Fetching current profile data !!!");
        progress2.show();
        storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageUriAccessToken = uri.toString();
                Picasso.get().load(uri).into(mImageView);
                progress2.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress2.dismiss();
            }
        });

        intent = getIntent();
        mEditText.setText(intent.getStringExtra("userName"));

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOperation();
            }
        });
    }

    private void updateOperation()
    {
        newName = mEditText.getText().toString();

        if(imagePath != null)
        {
            progressDialog.show();
            UserProfile userProfile = new UserProfile(newName, firebaseAuth.getUid());
            databaseReference.setValue(userProfile);

            updateImageToStorage();

            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.dismiss();
            Intent intent = new Intent(UpdateProfile.this, MainChatActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            progressDialog.show();
            UserProfile userProfile = new UserProfile(newName, firebaseAuth.getUid());
            databaseReference.setValue(userProfile);

            updateNameToCloudFireStore();

            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.dismiss();
            Intent intent = new Intent(UpdateProfile.this, MainChatActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void updateNameToCloudFireStore() {
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String , Object> userdata=new HashMap<>();
        userdata.put("name",newName);
        userdata.put("image",imageUriAccessToken);
        userdata.put("uid",firebaseAuth.getUid());
        userdata.put("status","Online");

        documentReference.set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(),"Profile Update Successfully",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateImageToStorage() {
        StorageReference imageRef = storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic");
        //Image Compression

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);

        }
        catch (Exception e){
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray(); //converting image into byte array

        //Putting 'data'(i.e. image in terms of bytes) into storage
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUriAccessToken = uri.toString();
                        Toast.makeText(getApplicationContext(), "URI get success",Toast.LENGTH_SHORT).show();
                        updateNameToCloudFireStore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "URL get failed", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(getApplicationContext(),"Image is Uploaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Image not Uploaded", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imagePath = data.getData();
            mImageView.setImageURI(imagePath);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("status","Offline");
    }

    @Override
    protected void onStart() {
        super.onStart();
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("status","Online");
    }
}