package com.nutscape.mc.nunuubot.modules;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.BotInterface;

public class VoteModule extends Module
{
    public VoteModule(IRC irc,BotInterface bot) 
        throws ModuleInstantiationException{
        super(irc,bot);
    }

    @Override
    public void privMsg(IncomingMessage m) {
        // TODO
    }
}
