package com.example.rushikesh.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    EditText e_name,e_email,e_pass;
    Button reg;

    FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    DatabaseReference databaseReference;
    Firebase firebase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        //firebase = new Firebase("https://postit-9eea6.firebaseio.com/Users");

       databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        progressDialog = new ProgressDialog(this);

        e_name = (EditText)findViewById(R.id.name);
        e_email = (EditText)findViewById(R.id.email);
        e_pass = (EditText)findViewById(R.id.pass);

        reg = (Button)findViewById(R.id.register);

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //databaseReference.child("ID").setValue("HI");
               reguser();
            }
        });
    }

    private void reguser()
    {
        final String name = e_name.getText().toString().trim();
        String email = e_email.getText().toString().trim();
        String pass = e_pass.getText().toString().trim();

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass))
        {

            progressDialog.setMessage("Signing In...");
            progressDialog.show();

            firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {


                    if(task.isSuccessful())
                    {

                        String UID = firebaseAuth.getCurrentUser().getUid();
                        DatabaseReference current_user = databaseReference.child(UID);
                        current_user.child("Name").setValue(name);
                        current_user.child("Image").setValue("default");

                        progressDialog.dismiss();

                        startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                    }

                }
            });
        }
    }
}
