package com.larkintuckerllc.oldschool;


public class Todo {

    private long mId;
    private String mName;
    private long mDate;

    public Todo(long id, String name, long date) {
        mId = id;
        mName = name;
        mDate = date;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public long getDate() {
        return mDate;
    }

}
