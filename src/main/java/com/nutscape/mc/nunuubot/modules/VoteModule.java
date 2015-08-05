package com.nutscape.mc.nunuubot.modules;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.ModuleConfig;

public class VoteModule extends Module
{
    public VoteModule(IRC irc,ModuleConfig config) {
        super(irc,config);
    }

    @Override
    public void privMsg(IncomingMessage m) {
        // TODO
    }
}
