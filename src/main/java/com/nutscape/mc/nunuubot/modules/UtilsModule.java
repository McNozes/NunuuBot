package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.CTCP;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.BotInterface;
import com.nutscape.mc.nunuubot.NoticeReceiver;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.ActionContainer;
import com.nutscape.mc.nunuubot.actions.CommandFactory;

public class UtilsModule extends Module implements NoticeReceiver
{
    private CTCP ctcp;
    private final ActionContainer commands = new ActionContainer();
    private final ActionContainer replies =  new ActionContainer();

    private void addPair(Action pair) {
        commands.add(pair);
        replies.add(pair);
    }

    public UtilsModule(IRC irc,BotInterface bot) {
        super(irc,bot);
        this.ctcp = new CTCP(irc);

        CommandFactory fac = new CommandFactory(bot.getCmdPrefix());

        // Add CTCP queries

        CTCP.Query[] supportedQueries = new CTCP.Query[] {
            CTCP.Query.VERSION,
            CTCP.Query.TIME,
            CTCP.Query.CLIENTINFO
        };
        for (CTCP.Query query : supportedQueries) {
            String word = query.toString();  // case is not important
            Pattern replyPat = query.replyPattern;
            Action cAct = new Action() {
                @Override
                public boolean accept(IncomingMessage m,String... args) {
                    ctcp.query(query,args[0]);
                    return true;
                }
            };
            Action rAct = new Action() {
                @Override
                public boolean accept(IncomingMessage m,String... args) {
                    String arg = CTCP.getArgs(query,m.getContent());
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
                ctcp.query(CTCP.Query.PING,args[0],timestamp.toString());
                return true;
            }
        };
        Action pingReply = new Action() {
            @Override
            public boolean accept(IncomingMessage m,String... args) {
                // TODO: move
                String arg = CTCP.getArgs(CTCP.Query.PING,m.getContent());
                Long sentTime = Long.valueOf(arg);
                Long delta = m.getTimestamp() - sentTime;
                irc.sendPrivMessage(args[0],m.getNick() + ": " + delta + "ms");
                return true;
            }
        };
        addPair(fac.newQueryCommand("ping",
                    CTCP.Query.PING.replyPattern,ping,pingReply));

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
                    CTCP.Query.PING.replyPattern,ping,hostReply));
    }

    // ---------

    @Override
    public void privMsg(IncomingMessage m) {
        commands.acceptAndReturnAtMatch(m);
    }

    @Override
    public void notice(IncomingMessage m) {
        replies.acceptAndReturnAtMatch(m);
    }
}

