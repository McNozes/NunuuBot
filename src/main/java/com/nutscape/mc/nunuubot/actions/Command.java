package com.nutscape.mc.nunuubot.actions;

import com.nutscape.mc.nunuubot.IncomingMessage;

public interface Command {
    boolean accept(IncomingMessage m);
}

