package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
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
    public void privMsg(IncomingMessage m) {
        String dest = m.getDestination();
        String nick = m.getNick();
        String msg = m.getContent();

        if (helloPattern.matcher(msg).matches()) {
            irc.sendPrivMessage(dest,"Hello, " + nick + "!");
        } else if (olaPattern.matcher(msg).matches()) {
            irc.sendPrivMessage(dest,"Olá, " + nick + "!");
        }
    }
}
