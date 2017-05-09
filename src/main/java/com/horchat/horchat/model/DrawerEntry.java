package com.horchat.horchat.model;

public class DrawerEntry implements DrawerItem {
    private int mEntryType;
    private String mEntryName;
    private int mEntryIcon;
    private int mSpecialId;
    private boolean mSelected;
    public DrawerEntry(String entryName, int entryIcon, boolean selected) {
        mEntryName = entryName;
        mEntryIcon = entryIcon;
        mSelected = selected;
    }
    public DrawerEntry(String entryName, int entryIcon) {
        mEntryName = entryName;
        mEntryIcon = entryIcon;
    }
    public DrawerEntry(String entryName) {
        mEntryName = entryName;
        mEntryIcon = 0;
    }
    public String getItemName() {
        return mEntryName;
    }
    public void setItemName(String entryName) {
        mEntryName = entryName;
    }
    public int getItemImage() {
        return mEntryIcon;
    }
    public void setItemImage(int entryImage) {
        this.mEntryIcon = entryImage;
    }
    public boolean isSection() {
        return false;
    }
    public boolean hasIcon() {
        return mEntryIcon != 0;
    }
    public boolean isSelected() {
        return mSelected;
    }
}
