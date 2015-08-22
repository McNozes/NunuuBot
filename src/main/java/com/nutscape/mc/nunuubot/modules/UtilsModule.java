package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IRC.CTCP;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.BotInterface;
import com.nutscape.mc.nunuubot.NoticeReceiver;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.ActionContainer;
import com.nutscape.mc.nunuubot.actions.CommandFactory;

public class UtilsModule extends Module implements NoticeReceiver
{
    private void addPair(Action pair) {
        addCommand(pair);
        addNotice(pair);
    }

    public UtilsModule(IRC irc,BotInterface bot) 
        throws ModuleInstantiationException {
        super(irc,bot);
        CommandFactory fac = new CommandFactory(bot.getCmdPrefix());

        // Add CTCP queries

        CTCP[] supportedQueries = new CTCP[] {
            CTCP.VERSION,
            CTCP.TIME,
            CTCP.CLIENTINFO
        };
        for (CTCP query : supportedQueries) {
            String word = query.toString();  // case is not important
            Pattern replyPat = query.replyPattern;
            Action cAct = new Action() {
                @Override
                public boolean accept(IncomingMessage m,String... args) {
                    irc.query(query,args[0]);
                    return true;
                }
            };
            Action rAct = new Action() {
                @Override
                public boolean accept(IncomingMessage m,String... args) {
                    String arg = query.getArgs(m.getContent());
                    irc.sendPrivMessage(args[0],m.getNick() + ": " + arg);
                    return true;
                }
            };
            addPair(fac.newQueryCommand(word,replyPat,cAct,rAct));
        }

        // PING

        Action ping = new Action() {
            @Override
            public boolean accept(IncomingMessage m,String... args) {
                Long timestamp = System.currentTimeMillis();
                irc.query(CTCP.PING,args[0],timestamp.toString());
                return true;
            }
        };

        Action pingReply = new Action() {
            @Override
            public boolean accept(IncomingMessage m,String... args) {
                // TODO: move
                String arg = CTCP.PING.getArgs(m.getContent());
                Long sentTime = Long.valueOf(arg);
                Long delta = m.getTimestamp() - sentTime;
                irc.sendPrivMessage(args[0],m.getNick() + ": " + delta + "ms");
                return true;
            }
        };

        addPair(fac.newQueryCommand("ping",
                    CTCP.PING.replyPattern,ping,pingReply));

        // HOST

        /* The host command just sends a ping to the target, and then
         * extracts the host from the prefix of the reply. */
        Action hostReply = new Action() {
            @Override
            public boolean accept(IncomingMessage m,String... args) {
                irc.sendPrivMessage(args[0],m.getNick() + ": " + m.getHost());
                return true;
            }
        };
        addPair(fac.newQueryCommand("host",
                    CTCP.PING.replyPattern,ping,hostReply));

        // WHOIS

        Action sendWhois = new Action() {
            @Override
            public boolean accept(IncomingMessage m,String... args) {
                irc.whois(args[0]);
                return true;
            }
        };
        addPair(fac.newUserCommand("whois",sendWhois));
        // DEBUG_BEGIN
        System.out.println("Debug:");
        // DEBUG_END
    }
}

