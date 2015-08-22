package com.nutscape.mc.nunuubot.modules;

import com.nutscape.mc.nunuubot.BotInterface;
import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.ActionContainer;
import com.nutscape.mc.nunuubot.actions.CommandFactory;

public class WorldTimeModule extends Module {
    public WorldTimeModule(IRC irc,BotInterface bot)
        throws ModuleInstantiationException
    {
        super(irc,bot);
        CommandFactory fac = new CommandFactory(bot.getCmdPrefix());
        //addComand(fac.newActionPattern());
    }

    class WorldTime extends Action {
        @Override
        public boolean accept(IncomingMessage m,String... args) {
            return true;
        }
    }
}

