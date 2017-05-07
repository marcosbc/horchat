package com.horchat.horchat.model;

public class TargetItem {
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_CHANNEL = 1;

    private int mType;
    private String mName;

    public TargetItem(String name) {
        mType = TYPE_DEFAULT;
        mName = name;
    }
    public String getName() {
        return mName;
    }
    public int getType() {
        return mType;
    }
}
