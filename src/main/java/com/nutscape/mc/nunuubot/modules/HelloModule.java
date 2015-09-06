package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;

import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.Bot;
import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.CommandFactory;
import com.nutscape.mc.nunuubot.actions.ActionContainer;

public class HelloModule extends Module
{
    public HelloModule(Bot bot) 
        throws ModuleInstantiationException {
        super(bot);

        Action olaAction = new Action() {
            @Override
            public boolean accept(IncomingMessage m,String... args) {
                bot.getIRC().sendPrivMessage(
                        m.getDestination(),"Olái, " + m.getNick()+"!");
                return true;
            }
        };

        Action helloAction = new Action() {
            @Override
            public boolean accept(IncomingMessage m,String... args) {
                bot.getIRC().sendPrivMessage(
                        m.getDestination(),"Hello, " + m.getNick() + "!");
                return true;
            }
        };

        String hps = " *hello[ ,]* +" + bot.getNickname() + "[!.]*";
        String ops = " *[oO][lL][aáAÁ][ ,]* +" + bot.getNickname() + "[!.]*";

        CommandFactory fac = new CommandFactory(bot.getCmdPrefix());
        fac.setIRC(bot.getIRC());
        addCommand(fac.newActionPattern(
                    Pattern.compile(hps,Pattern.CASE_INSENSITIVE),olaAction));
        addCommand(fac.newActionPattern(
                    Pattern.compile(ops,Pattern.CASE_INSENSITIVE),helloAction));
    }
}
