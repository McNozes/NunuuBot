package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.ModuleConfig;

public class LinkModule extends Module
{
    public LinkModule(IRC irc,ModuleConfig config) {
        super(irc,config);
    }

    private static final Pattern youtubePattern = Pattern.compile(
            "\\s*(https?://)?(www.)?youtube\\.com/watch\\?v=.*");

    //private static final Pattern imgur;

    private static final Pattern snapPattern  = Pattern.compile(
                "\\s*(New snap:)?\\s*(https?://)?(www.)?european\\.shitposting\\.agency/snaps/.*");

    @Override
    public void privMsg(String prefix,String dest,String msg) {
        if (snapPattern.matcher(msg).matches()) {
            irc.sendPrivMessage(dest,
                    msg.replaceAll("european\\.shitposting\\.agency","95.85.2.91"));
        }
    }
}
