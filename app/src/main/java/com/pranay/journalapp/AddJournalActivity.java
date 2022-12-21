package com.pranay.journalapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pranay.journalapp.model.Journal;


import java.util.Date;

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
        firebaseAuth = FirebaseAuth.getInstance();
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
        authStateListener= firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
            if(user != null){

            }else {

            }
        };

        saveButton.setOnClickListener(v -> SaveJournal());
        addPhotoButton.setOnClickListener(v -> {
            //Getting image from gallery
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent,GALLERY_CODE);

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
            filepath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String imageUrl = uri.toString();
                    //Creating objects of journal
                    Journal journal = new Journal();
                    journal.setTitle(title);
                    journal.setThoughts(thoughts);
                    journal.setImageUrl(imageUrl);
                    journal.setTimeAdded(new Timestamp(new Date()));
                    journal.setUserName(currentUsername);
                    journal.setUserId(currentUserId);


                    //Invoking collection reference
                    collectionReference.add(journal)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    startActivity(new Intent(AddJournalActivity.this
                                            ,JournalListActivity.class
                                            ));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(),"Failed: "
                                            +e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            })).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.VISIBLE);

                }
            });

        }else{
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK){
            if(data != null){
                imageUri = data.getData();//getting the actual path
                imageView.setImageURI(imageUri);//Showing the image
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}