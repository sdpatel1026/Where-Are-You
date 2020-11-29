package com.sd.whereareyou.models;

public class FriendBlock {

    private String userName;
    private String UID;
    private String time;


    public FriendBlock(String name, String UID, String time) {
        this.userName = name;
        this.UID = UID;
        this.time = time;
    }

    public String getUserName() {
        return userName;
    }

    public String getUID() {
        return UID;
    }

    public String getTime() {
        return time;
    }


}
