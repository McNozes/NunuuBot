package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.BotInterface;
import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.BotInterface;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.CommandFactory;
import com.nutscape.mc.nunuubot.actions.ActionContainer;


public class HelloModule extends Module
{
    private final ActionContainer commands = new ActionContainer();

    public HelloModule(IRC irc,BotInterface bot) {
        super(irc,bot);

        CommandFactory fac = new CommandFactory(bot.getCmdPrefix());
        fac.setIRC(irc);
        commands.add(fac.newCommand("ol[aá]",olaAction));
    }

    private final Pattern helloPattern =
        Pattern.compile(" *hello[ ,]* +" + bot.getNickname() +
                "[!.]*",Pattern.CASE_INSENSITIVE);

    private final Pattern olaPattern =
        Pattern.compile(" *[oO][lL][aáAÁ][ ,]* +" + bot.getNickname() +
                "[!.]*",Pattern.CASE_INSENSITIVE);

    private Action olaAction = new Action() {
        @Override
        public boolean accept(IncomingMessage m,String... args) {
            irc.sendPrivMessage(m.getDestination(),"Olá, " + m.getNick()+"!");
            return true;
        }
    };

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

        commands.acceptAndReturnAtMatch(m);
    }
}
