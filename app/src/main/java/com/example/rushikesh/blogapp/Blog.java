package com.example.rushikesh.blogapp;

/**
 * Created by Dell on 24/12/2016.
 */
public class Blog {
    private String title,image,desc,username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Blog()
    {

    }
    public Blog(String title, String image, String desc,String username) {
        this.title = title;
        this.image = image;
        this.desc = desc;
        this.username=username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
