package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;

import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.Bot;
import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;

public class LinkModule extends Module
{
    public LinkModule(Bot bot) 
        throws ModuleInstantiationException {
        super(bot);
    }

    private static final Pattern youtubePattern = Pattern.compile(
            "\\s*(https?://)?(www.)?youtube\\.com/watch\\?v=.*");

    //private static final Pattern imgur;

    private static final Pattern snapPattern  = Pattern.compile(
                "\\s*(New snap:)?\\s*(https?://)?(www.)?european\\.shitposting\\.agency/snaps/.*");
}
