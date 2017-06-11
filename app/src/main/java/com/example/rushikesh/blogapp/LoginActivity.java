package com.example.rushikesh.blogapp;

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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText e_email,e_pass;
    private FirebaseAuth firebaseAuth;
    private Firebase firebase;
    private Button sign;
    private SignInButton signInButton;
    private TextView createacc;
    private static final int RC_SIGN_IN=1;
    private static final String TAG="LogIn Activity";
    private GoogleApiClient googleApiClient;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressDialog = new ProgressDialog(this);

        e_email = (EditText)findViewById(R.id.e_mail);
        e_pass = (EditText)findViewById(R.id.password);
        signInButton = (SignInButton)findViewById(R.id.google);

        sign = (Button)findViewById(R.id.button3);

        createacc = (TextView)findViewById(R.id.textView3);


        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.keepSynced(true);

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });




        createacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });


        //------GOOGLE SIGNIN--------



        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
     //   Toast.makeText(LoginActivity.this,"Login",Toast.LENGTH_LONG).show();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(LoginActivity.this,"Login",Toast.LENGTH_LONG).show();
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);


            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                progressDialog.setMessage("Signing In...");
                progressDialog.show();
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...

                progressDialog.dismiss();
            }
        }
        else {
            Toast.makeText(LoginActivity.this,"FAiled",Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            progressDialog.dismiss();
                            checkUserExists();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }


                    }


                });
    }


    private void checkLogin() {

        String email = e_email.getText().toString().trim();
        String pass = e_pass.getText().toString().trim();

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass))
        {
            progressDialog.setMessage("Loging In...");
            progressDialog.show();
            firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        progressDialog.dismiss();
                        checkUserExists();
                    }
                    else
                    {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this,"Error In Signing In",Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }

    }

    private void checkUserExists() {

       if(firebaseAuth.getCurrentUser()!=null)
       {
           final String UID = firebaseAuth.getCurrentUser().getUid();

           databaseReference.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {

                   if (dataSnapshot.hasChild(UID)) {
                       Intent main = new Intent(LoginActivity.this, MainActivity.class);
                       main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                       startActivity(main);
                   } else {
                       Intent setup = new Intent(LoginActivity.this, SetupActivity.class);
                       setup.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                       startActivity(setup);
                   }

               }
               @Override
               public void onCancelled(DatabaseError databaseError) {

               }
           });
       }
    }
}
