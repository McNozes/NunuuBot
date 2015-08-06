package com.nutscape.mc.nunuubot.modules;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.BotInterface;

public class LastfmModule extends Module
{
    public LastfmModule(IRC irc,BotInterface bot) {
        super(irc,bot);
    }

    @Override
    public void privMsg(IncomingMessage m) {
        // TODO
    }
}

