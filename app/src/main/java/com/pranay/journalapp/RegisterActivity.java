package com.pranay.journalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    EditText email_create,password_create,username_create;
    Button registerButton;

    //Firebase Authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //FireStore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        email_create = findViewById(R.id.etRegEmail);
        password_create = findViewById(R.id.etRegPass);
        registerButton = findViewById(R.id.btnRegister);
        username_create = findViewById(R.id.etRegUsername);

        //Authentication
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if(currentUser !=null){
                    //user already have account

                }else{
                    //User does not have account yet

                }
            }
        };

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(email_create.getText().toString())
                && !TextUtils.isEmpty(password_create.getText().toString())){
                    String email = email_create.getText().toString().trim();
                    String password = password_create.getText().toString().trim();
                    String username = username_create.getText().toString().trim();
                    CreateUserEmailAccount(email,password,username);
                }else{
                    Toast.makeText(RegisterActivity.this
                            ,"Empty fields!!!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void CreateUserEmailAccount(String email, String password,final String username) {
        if(!TextUtils.isEmpty(email_create.getText().toString())
                && !TextUtils.isEmpty(password_create.getText().toString())){
            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //We take user to next activity

                                currentUser = firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                final  String currentUserId = currentUser.getUid();

                                //Creating a UserMap so we can create user data in the firestore collection
                                Map<String,String> userObject = new HashMap<>();
                                userObject.put("userId",currentUserId);
                                userObject.put("username",username);

                                //Adding user to Firestore
                                collectionReference.add(userObject)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                             documentReference.get()
                                                     .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                         @Override
                                                         public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                             if(Objects.requireNonNull(task.getResult().exists())){
                                                                 String name = task.getResult().getString("username");
                                                                 //After account is created now going to addjournal activity
                                                                 Intent i = new Intent(RegisterActivity.this
                                                                         , AddJournalActivity.class);
                                                                 i.putExtra("username",name);
                                                                 i.putExtra("userId",currentUserId);
                                                                 startActivity(i);

                                                             }else {

                                                             }
                                                         }
                                                     }).addOnFailureListener(new OnFailureListener() {
                                                         @Override
                                                         public void onFailure(@NonNull Exception e) {
                                                             Toast.makeText(RegisterActivity.this,"Something Went Wrong!!"
                                                             ,Toast.LENGTH_SHORT).show();
                                                         }
                                                     });
                                            }
                                        });

                            }
                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}