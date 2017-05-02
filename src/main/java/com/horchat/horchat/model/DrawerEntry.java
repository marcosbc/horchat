package com.horchat.horchat.model;

public class DrawerEntry implements DrawerItem {
    private int mEntryType;
    private String mEntryName;
    private int mEntryIcon;
    private int mSpecialId;
    public DrawerEntry(String entryName, int entryIcon) {
        super();
        mEntryName = entryName;
        mEntryIcon = entryIcon;
    }
    public DrawerEntry(String entryName) {
        super();
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
}
