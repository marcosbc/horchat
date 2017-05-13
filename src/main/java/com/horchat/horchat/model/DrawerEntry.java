package com.horchat.horchat.model;

public class DrawerEntry implements DrawerItem {
    private String mEntryName;
    private int mEntryIcon;
    private boolean mSelected;
    private boolean mIsRead;
    public DrawerEntry(String entryName, int entryIcon, boolean isRead, boolean selected) {
        mEntryName = entryName;
        mEntryIcon = entryIcon;
        mIsRead = isRead;
        mSelected = selected;
    }
    public DrawerEntry(String entryName, int entryIcon) {
        mEntryName = entryName;
        mEntryIcon = entryIcon;
        mIsRead = true;
    }
    public DrawerEntry(String entryName) {
        mEntryName = entryName;
        mEntryIcon = 0;
        mIsRead = true;
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
    public boolean isRead() {
        return mIsRead;
    }
}
