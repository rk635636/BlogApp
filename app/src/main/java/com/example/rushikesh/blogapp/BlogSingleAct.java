package com.example.rushikesh.blogapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BlogSingleAct extends AppCompatActivity {

    private ImageView imageView;
    private TextView p_title,p_desc,p_user;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private Button remove_bt;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        imageView = (ImageView)findViewById(R.id.ip);
        p_title = (TextView)findViewById(R.id.tp);
        p_desc = (TextView)findViewById(R.id.dp);
        p_user = (TextView)findViewById(R.id.user_name);
        remove_bt = (Button)findViewById(R.id.remove);

        final String post_id = getIntent().getExtras().getString("blog_id");


        databaseReference.child(post_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String title = dataSnapshot.child("title").getValue().toString();
                String desc = dataSnapshot.child("desc").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String uid = dataSnapshot.child("uid").getValue().toString();
                String user = dataSnapshot.child("username").getValue().toString();

                p_title.setText(title);
                p_desc.setText(desc);
                p_user.setText("Posted By "+user );

                Picasso.with(BlogSingleAct.this).load(image).into(imageView);

                if(auth.getCurrentUser().getUid().equals(post_id))
                {
                    remove_bt.setVisibility(View.VISIBLE);
                }
                else
                {
                    remove_bt.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        remove_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(BlogSingleAct.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(BlogSingleAct.this);
                }

                builder.setTitle("Remove Post")
                        .setMessage("Are you sure you want to Remove this Post?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                databaseReference.child(post_id).removeValue();

                                startActivity(new Intent(BlogSingleAct.this,MainActivity.class));
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                            }
                        })
                        .show();



            }
        });

    }


}
