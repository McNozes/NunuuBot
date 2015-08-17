package com.nutscape.mc.nunuubot;

import java.util.logging.Level;

// TODO use singleton pattern with abstract class
public interface BotInterface {
    String getNickname();
    char getSpecialChar();
    String getCmdPrefix();
    String getModuleDataDir();
    void log(Level level,String msg);
    void logThrowable(Throwable e);
}
