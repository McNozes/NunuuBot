package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IRC.CTCP;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.Bot;
import com.nutscape.mc.nunuubot.NoticeReceiver;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.ActionContainer;
import com.nutscape.mc.nunuubot.actions.CommandFactory;

public class UtilsModule extends Module
{
    private void addPair(Action pair) {
        addPrivMsgAction(pair);
        addNoticeAction(pair);
    }

    public UtilsModule(Bot bot) 
        throws ModuleInstantiationException {
        super(bot);
        CommandFactory fac = new CommandFactory(bot);

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
                    bot.getIRC().query(query,args[0]);
                    return true;
                }
            };
            Action rAct = new Action() {
                @Override
                public boolean accept(IncomingMessage m,String... args) {
                    String arg = query.getArgs(m.getContent());
                    bot.getIRC().sendPrivMessage(
                            args[0],m.getNick() + ": " + arg);
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
                bot.getIRC().query(CTCP.PING,args[0],timestamp.toString());
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
                bot.getIRC().sendPrivMessage(
                        args[0],m.getNick() + ": " + delta + "ms");
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
                bot.getIRC().sendPrivMessage(
                        args[0],m.getNick() + ": " + m.getHost());
                return true;
            }
        };
        addPair(fac.newQueryCommand("host",
                    CTCP.PING.replyPattern,ping,hostReply));

        // WHOIS
        Action sendWhois = new Action() {
            @Override
            public boolean accept(IncomingMessage m,String... args) {
                bot.getIRC().whois(args[0]);
                return true;
            }
        };
        addPair(fac.newUserCommand("whois",sendWhois));

        // AUTO-JOIN
        Action autoJoin = new Action() {
            @Override
            public boolean accept(IncomingMessage m,String... args) {
                String parts[] = m.getContent().split(" ");
                if (parts[0].equals(bot.getNickname())) {
                    bot.getIRC().join(m.getDestination());
                }
                return true;
            }
        };
        //String kickRegex = "KICK +[^ ]+ +[ ^]( +:.*)?";
        addKickAction(fac.newPatternAction(Pattern.compile(".*"),autoJoin));

        // AUTO-CTCP RESPOND

        Action versionRespond = new Action() {
            @Override
            public boolean accept(IncomingMessage m,String... args) {
                String dest = m.getNick();
                if (dest == null) {
                    dest = m.getPrefix();
                }
                bot.getIRC().sendNotice(dest,"\001VERSION " + 
                        bot.getVersion() + "\001");
                return true;
            }
        };
        
        addPair(fac.newPatternAction(
                    Pattern.compile("\001VERSION\001"),versionRespond));
    }
}

