package com.nutscape.mc.nunuubot.modules.utils;

public interface ReplyPattern extends CommandPattern {
    boolean acceptReply(String prefix,String dest,String msg,long t);
}
