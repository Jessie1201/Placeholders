package com.pdp.orthex.fragmentedapplication;

import java.util.ArrayList;

public class User {
    String userid;
    String username;
    ArrayList<String> userlist;

    public User(){

    }

    public User(String userid, String username, ArrayList<String> userlist){
        this.userid = userid;
        this.username = username;
        this.userlist = userlist;

    }


    public String getUserid() {
        return userid;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<String> getUserlist() {
        return userlist;
    }

}
