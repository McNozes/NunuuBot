package com.nutscape.mc.nunuubot;

public interface NoticeReceiver {
    void notice(String prefix,String dest,String msg,long t);
}
