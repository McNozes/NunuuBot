package com.nutscape.mc.nunuubot;

public class ChannelUser {
    public final long mJoinTime;
    private int mTokens;

    ChannelUser(long joinTime) {
        mJoinTime = joinTime;
        mTokens = 0;
    }

    ChannelUser(long joinTime,int tokens){
        this(joinTime);
        mTokens = tokens;
    }

    public void addTokens(int val) {
        mTokens += val;
    }

    public void rmTokens(int val) {
        mTokens -= val;
    }

    public int getTokens() {
        return mTokens;
    }
}
