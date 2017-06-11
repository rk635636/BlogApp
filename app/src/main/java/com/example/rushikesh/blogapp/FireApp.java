package com.example.rushikesh.blogapp;
import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by rushikesh on 31/5/17.
 */
public class FireApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);//The Firebase library must be initialized once with an Android Context
    }
}
