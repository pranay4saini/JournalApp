package com.pranay.journalapp;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.QueryDocumentSnapshot;



import Util.JournalUser;

public class MainActivity extends AppCompatActivity {

    Button logInButton;
    TextView register;
    EditText emailET,passET;
    //Firebase Authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    //FireStore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logInButton = findViewById(R.id.btnLogin);
        register = findViewById(R.id.tvRegisterHere);
        emailET = findViewById(R.id.etLoginEmail);
        passET = findViewById(R.id.etLoginPass);
        firebaseAuth = FirebaseAuth.getInstance();

        register.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(i);
        });

        logInButton.setOnClickListener(v -> LoginEmailPasswordUser(emailET.getText().toString().trim(),
                passET.getText().toString().trim()));
    }

    private void LoginEmailPasswordUser(String email, String password) {
        //checking for empty fields
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            firebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(task -> {

                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        assert user != null;
                        final String currentUserId = user.getUid();
                        collectionReference.whereEqualTo("userId",currentUserId)
                                .addSnapshotListener((value, error) -> {
                                    if(error != null){

                                    }
                                    assert value != null;
                                    if(!value.isEmpty()){
                                        //getting  all querydocsnapshot
                                        for(QueryDocumentSnapshot snapshot : value){
                                            JournalUser journalUser = JournalUser.getInstance();
                                            journalUser.setUsername(snapshot.getString("username"));
                                            journalUser.setUserId(snapshot.getString("userId"));

                                            //Going to ListActivity after succesful login
                                            startActivity(new Intent(MainActivity.this, JournalListActivity.class));
                                        }
                                    }
                                });
                    }).addOnFailureListener(e -> Toast.makeText(MainActivity.this,"Something went Wrong",Toast.LENGTH_SHORT).show());
        }else{
            Toast.makeText(MainActivity.this,"Please Enter Email and password ",Toast.LENGTH_SHORT).show();
        }
    }
}