package com.example.rushikesh.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

//1.What is Uri
public class PostActivity extends AppCompatActivity {

    private EditText title;
    private EditText desc;
    private Button post;
   // private Uri uri=null;
    private StorageReference storage;
    private ImageButton imageButton;
    private ProgressDialog progress;
    private DatabaseReference databaseReference;
    private DatabaseReference Users;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mauth;
    private static final int GALLERY_REQUEST=1;
    private Uri imageuri=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        imageButton = (ImageButton)findViewById(R.id.image);
        title = (EditText)findViewById(R.id.tp);
        desc = (EditText)findViewById(R.id.desc);
        post = (Button)findViewById(R.id.postit);

        mauth = FirebaseAuth.getInstance();
        firebaseUser = mauth.getCurrentUser();

        progress = new ProgressDialog(this);
        storage = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        Users = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();


            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,GALLERY_REQUEST);
            }
        });
    }



    public void startPosting() {

        progress.setMessage("Posting...");
        progress.show();

        final String title_text = title.getText().toString().trim();
        final String desc_text = desc.getText().toString().trim();

        if(!TextUtils.isEmpty(title_text) && !TextUtils.isEmpty(desc_text) && imageuri!=null)
        {

            StorageReference file_path = storage.child("Blog_Imgs").child(imageuri.getLastPathSegment());//uri.getLastPathSegment() returns image name we can use any random name as well
            file_path.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri down_url = taskSnapshot.getDownloadUrl();//GEt Download Ulr of Image

                    final DatabaseReference new_post = databaseReference.push();


                    Users.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            new_post.child("title").setValue(title_text);
                            new_post.child("desc").setValue(desc_text);
                            new_post.child("image").setValue(down_url.toString());
                            new_post.child("uid").setValue(firebaseUser.getUid());
                            new_post.child("username").setValue(dataSnapshot.child("Name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                    {

                                        startActivity(new Intent(PostActivity.this,MainActivity.class));

                                    }

                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    progress.dismiss();


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this,"Error", Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK)
        {
            Uri uri = data.getData();
            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAutoZoomEnabled(true)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageuri = result.getUri();

                imageButton.setImageURI(imageuri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
