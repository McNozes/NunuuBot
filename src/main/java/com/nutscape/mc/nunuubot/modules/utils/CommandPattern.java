package com.nutscape.mc.nunuubot.modules.utils;

public interface CommandPattern {
    boolean acceptCommand(String prefix,String dest,String msg,long t);
}

