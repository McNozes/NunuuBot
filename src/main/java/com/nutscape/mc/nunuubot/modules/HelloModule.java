package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.ModuleConfig;

public class HelloModule extends Module
{
    public HelloModule(IRC irc,ModuleConfig config) {
        super(irc,config);
    }

    private final Pattern helloPattern =
        Pattern.compile("[hH]ello[.]?");

    private final Pattern olaPattern =
        Pattern.compile("[oO][lL][aáAÁ][ ,]* +" + config.getNickname());

    @Override
    public void privMsg(String prefix,String dest,String msg,long t) {
        String nick = prefix.split("!")[0];
        if (helloPattern.matcher(msg).matches()) {
            irc.sendPrivMessage(dest,"Hello, " + nick + "!");
        } else if (olaPattern.matcher(msg).matches()) {
            irc.sendPrivMessage(dest,"Olá, " + nick + "!");
        }
    }
}
