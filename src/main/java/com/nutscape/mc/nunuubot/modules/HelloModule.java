package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.BotInterface;

public class HelloModule extends Module
{
    public HelloModule(IRC irc,BotInterface bot) {
        super(irc,bot);
    }

    private final Pattern helloPattern =
        Pattern.compile(" *hello[ ,]* +" + bot.getNickname() +
                "[!.]*",Pattern.CASE_INSENSITIVE);

    private final Pattern olaPattern =
        Pattern.compile(" *[oO][lL][aáAÁ][ ,]* +" + bot.getNickname() +
                "[!.]*",Pattern.CASE_INSENSITIVE);

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
