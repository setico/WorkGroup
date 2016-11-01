package com.codelab.workgroup.model;

/**
 * Created by setico on 10/31/16.
 */
public class Topic {
    private String key;
    private String user;
    private String title;

    public Topic() {
    }

    public Topic(String key, String user, String title) {
        this.key = key;
        this.user = user;
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
