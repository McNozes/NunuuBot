package com.nutscape.mc.nunuubot.modules;

import com.nutscape.mc.nunuubot.Bot;
import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.ActionContainer;
import com.nutscape.mc.nunuubot.actions.CommandFactory;

public class TimeModule extends Module {
    public TimeModule(Bot bot)
        throws ModuleInstantiationException {
        super(bot);
    }

    class WorldTime extends Action {
        @Override
        public boolean accept(IncomingMessage m,String... args) {
            //ZoneId zone = ZoneId;
            return true;
        }
    }
}

