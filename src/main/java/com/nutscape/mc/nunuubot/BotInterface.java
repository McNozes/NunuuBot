package com.nutscape.mc.nunuubot;

import java.util.logging.Level;

public interface BotInterface {
    String getNickname();
    String getSpecialChar();
    String getCmdPrefix();
    void log(Level level,String msg);
    void logThrowable(Throwable e);
}
