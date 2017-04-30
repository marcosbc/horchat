package com.horchat.horchat.model;

public class DrawerSection implements DrawerItem {
    private String mSectionName;
    public DrawerSection(String sectionName) {
        super();
        mSectionName = sectionName;
    }
    public String getItemName() {
        return mSectionName;
    }
    public void setItemName(String sectionName) {
        mSectionName = sectionName;
    }
    public boolean isSection() {
        return true;
    }
}
