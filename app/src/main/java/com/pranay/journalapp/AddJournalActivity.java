package com.pranay.journalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;

import Util.JournalUser;

public class AddJournalActivity extends AppCompatActivity {

    private static final int GALLERY_CODE = 1;
    private Button  saveButton;
    private EditText titleEditText,descriptionEditText;
    private TextView currentUserTextview;
    private ImageView addPhotoButton,imageView;
    private ProgressBar progressBar;


    //UserId and username
    private String currentUserId;
    private String currentUsername;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;


    //Connection to firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("journal");
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal);

        storageReference = FirebaseStorage.getInstance().getReference();
        progressBar = findViewById(R.id.post_progressBar);
        titleEditText = findViewById(R.id.post_title_et);
        descriptionEditText = findViewById(R.id.post_description_et);
        currentUserTextview = findViewById(R.id.post_username_textview);
        imageView = findViewById(R.id.post_imageView);
        saveButton = findViewById(R.id.post_save_journal_button);
        addPhotoButton = findViewById(R.id.postCameraButton);

        progressBar.setVisibility(View.INVISIBLE);


        if(JournalUser.getInstance() !=null){
            currentUsername=JournalUser.getInstance().getUsername();
            currentUserId = JournalUser.getInstance().getUserId();
            currentUserTextview.setText(currentUsername);
        }
        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user != null){

                }else {

                }
            }
        };

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveJournal();
            }
        });
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Getting image from gallery
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_CODE);

            }
        });

    }


    private void SaveJournal() {
        final String title = titleEditText.getText().toString().trim();
        final String thoughts = descriptionEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        if(!TextUtils.isEmpty(title) &&!TextUtils.isEmpty(thoughts) && imageUri !=null){
            //saving path for  images in firebase
            final StorageReference filepath = storageReference.child("journal_images")
                    .child("my_image"+ Timestamp.now().getSeconds());

            //uploading the images
            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            //Creating objects of journal

                        }
                    })

                }
            });

        }
    }
}