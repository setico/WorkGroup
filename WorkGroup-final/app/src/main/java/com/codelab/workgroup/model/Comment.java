package com.codelab.workgroup.model;

/**
 * Created by setico on 10/31/16.
 */
public class Comment {
    private String user;
    private String userPhoto;
    private String comment;

    public Comment() {
    }

    public Comment(String user, String userPhoto, String comment) {
        this.user = user;
        this.userPhoto = userPhoto;
        this.comment = comment;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getuserPhoto() {
        return userPhoto;
    }

    public void setuserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
