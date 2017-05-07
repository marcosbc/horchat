package com.horchat.horchat.model;

public class Channel extends TargetItem {
    private static final int CHANNELLEN = 50;

    private int mType;
    private String mName;
    private int mUserCount;
    private String mTopic;
    public Channel(String name, int userCount, String topic) {
        super(name);
        mName = name;
        mUserCount = userCount;
        mTopic = topic;
        mType = TargetItem.TYPE_CHANNEL;
    }
    public String getName() {
        return mName;
    }
    public int getUserCount() {
        return mUserCount;
    }
    public String getTopic() {
        return mTopic;
    }
    public void update(Channel channel) {
        if (mName.equals(channel.getName())) {
            mUserCount = channel.getUserCount();
            mTopic = channel.getTopic();
        }
    }
    public int getType() {
        return mType;
    }
    public static boolean validate(String name) {
        boolean isValid = false;
        if (name != null && name.length() > 1
                && name.startsWith("#")
                && name.length() <= CHANNELLEN) {
            isValid = true;
        }
        return isValid;
    }
}
