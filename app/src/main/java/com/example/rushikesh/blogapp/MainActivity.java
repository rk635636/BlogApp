package com.example.rushikesh.blogapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView Bloglist;
    private DatabaseReference databaseReference;
    private DatabaseReference dataUsers;
    public FirebaseAuth auth;
    private  boolean process_like = false;
    private DatabaseReference dataLike;
  //  private String UID=null;
    public FirebaseAuth.AuthStateListener authStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();


        authStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() == null)
                {
                    Toast.makeText(MainActivity.this,"HIII",Toast.LENGTH_LONG).show();
                    Intent login = new Intent(MainActivity.this,LoginActivity.class);
                    login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(login);
                }
                else
                {

                }

            }
        };



        databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        dataUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        dataLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        dataUsers.keepSynced(true);
        databaseReference.keepSynced(true);
        dataLike.keepSynced(true);

        Bloglist=  (RecyclerView)findViewById(R.id.blog_list);

        Bloglist.setHasFixedSize(true);
        Bloglist.setLayoutManager(new LinearLayoutManager(this));

        checkUserExists();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();

       auth.addAuthStateListener(authStateListener);

        FirebaseRecyclerAdapter<Blog,BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(

                Blog.class,
                R.layout.blog_row,
                BlogViewHolder.class,
                databaseReference

        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(),model.getImage());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setLikeBt(post_key);

                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(MainActivity.this, BlogSingleAct.class);
                        intent.putExtra("blog_id",post_key);
                        startActivity(intent);

                    }
                });

                viewHolder.like_bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        process_like = true;



                            dataLike.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(process_like)
                                    {

                                    if (dataSnapshot.child(post_key).hasChild(auth.getCurrentUser().getUid())) {

                                        dataLike.child(post_key).child(auth.getCurrentUser().getUid()).removeValue();
                                        process_like = false;

                                    } else {
                                        dataLike.child(post_key).child(auth.getCurrentUser().getUid()).setValue("Random Value");
                                        process_like = false;
                                    }
                                }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });



                    }
                });

            }
        };


        Bloglist.setAdapter(firebaseRecyclerAdapter);
    }

    private void checkUserExists() {


       if(auth.getCurrentUser()!=null)
       {
           final String UID = auth.getCurrentUser().getUid();

           dataUsers.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {

                   if (!dataSnapshot.hasChild(UID)) {
                       Intent setup = new Intent(MainActivity.this, SetupActivity.class);
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

    public static class BlogViewHolder extends RecyclerView.ViewHolder
    {
        public View view;
        //TextView post_title;
        DatabaseReference datalike;
        FirebaseAuth auth;
        ImageButton like_bt;


        public BlogViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            like_bt = (ImageButton)view.findViewById(R.id.like);
            auth = FirebaseAuth.getInstance();
            datalike = FirebaseDatabase.getInstance().getReference().child("Likes");
            datalike.keepSynced(true);
        /*  post_title = (TextView) view.findViewById(R.id.tp);

            //On click For Individual Item

            post_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.v("MainActivity","Post Title");

                }
            });*/
        }

        public void setTitle(String title)
        {

            TextView post_title = (TextView) view.findViewById(R.id.tp);
            post_title.setText(title);
        }

        public void setDesc(String desc)
        {
            TextView post_desc = (TextView) view.findViewById(R.id.dp);
            post_desc.setText(desc);

        }
        public void setUsername(String username)
        {
            TextView user = (TextView) view.findViewById(R.id.user_name);
            user.setText(username);
        }

        public void setLikeBt(final String post_key)
        {
            datalike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child(post_key).hasChild(auth.getCurrentUser().getUid()))
                    {
                        like_bt.setImageResource(R.mipmap.red_like);

                    }
                    else
                    {
                        like_bt.setImageResource(R.mipmap.gray_like);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
       public void setImage(Context ctx, String image)
        {
            ImageView post_image = (ImageView) view.findViewById(R.id.ip);
            Picasso.with(ctx).load(image).into(post_image);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_add)
        {
            startActivity(new Intent(MainActivity.this,PostActivity.class));
        }

        else if(item.getItemId()==R.id.action_setings)
        {

        }
        else if (item.getItemId()==R.id.logout)
        {
            FirebaseAuth.getInstance().signOut();
        }
        return super.onOptionsItemSelected(item);
    }

}
