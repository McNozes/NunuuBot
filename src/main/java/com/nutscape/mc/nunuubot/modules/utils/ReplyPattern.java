package com.nutscape.mc.nunuubot.modules.utils;

import com.nutscape.mc.nunuubot.IncomingMessage;

public interface ReplyPattern extends CommandPattern {
    boolean acceptReply(IncomingMessage m);
}
