package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.CTCP;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.ModuleConfig;
import com.nutscape.mc.nunuubot.NoticeReceiver;
import com.nutscape.mc.nunuubot.modules.utils.Action;
import com.nutscape.mc.nunuubot.modules.utils.CommandPattern;
import com.nutscape.mc.nunuubot.modules.utils.ReplyPattern;
import com.nutscape.mc.nunuubot.modules.utils.QueryPairPattern;

public class UtilsModule extends Module implements NoticeReceiver
{
    private CTCP ctcp;

    private List<CommandPattern> commands;
    private List<ReplyPattern> replies;

    private void addQueryPair(QueryPairPattern pair) {
        commands.add(pair);
        replies.add(pair);
    }

    public UtilsModule(IRC irc,ModuleConfig config) {
        super(irc,config);
        this.ctcp = new CTCP(irc);

        this.commands = new ArrayList<CommandPattern>();
        this.replies = new ArrayList<ReplyPattern>();
        String cmdPrefix = config.getCmdPrefix();

        // Add CTCP queries

        for (CTCP.Query query : CTCP.Query.values()) {
            if (query == CTCP.Query.PING) {
                addQueryPair(new QueryPairPattern(cmdPrefix,"ping",
                            CTCP.Query.PING.replyPattern,
                            new PingAction(),new PingReplyAction()));
            } else {
                String word = query.toString();  // case is not important
                Pattern replyPattern = query.replyPattern;

                Action cAct = new Action() {
                    @Override
                    public void doAction(String target,String dest,
                            String msg,long t) {
                        ctcp.query(query,target);
                    }
                };

                Action rAct = new Action() {
                    @Override
                    public void doAction(String target,String dest,
                            String msg,long t) {
                        String arg = CTCP.getArgs(query,msg);
                        irc.sendPrivMessage(dest,target + ": " + arg);
                    }
                };

                addQueryPair(new QueryPairPattern(cmdPrefix,word,
                            replyPattern,cAct,rAct));
            }
        }
    }

    // ---------

    class PingAction extends Action {
        @Override
        public void doAction(String target,String dest,String msg,long t) {
            Long timestamp = System.currentTimeMillis();
            ctcp.query(CTCP.Query.PING,target,timestamp.toString());
        }
    }

    class PingReplyAction extends Action {
        @Override
        public void doAction(String nick,String channel,String msg,long t) {
            // TODO: move
            String arg = CTCP.getArgs(CTCP.Query.PING,msg);
            Long sentTime = Long.valueOf(arg);
            Long delta = t - sentTime;
            irc.sendPrivMessage(channel,nick + ": " + delta + "ms");
        }
    }

    @Override
    public void privMsg(String prefix,String dest,String msg,long t) {
        for (CommandPattern command : commands) {
            if (command.acceptCommand(prefix,dest,msg,t))
                return;
        }
    }

    @Override
    public void notice(String prefix,String dest,String msg,long t) {
        for (ReplyPattern reply : replies) {
            if (reply.acceptReply(prefix,dest,msg,t))
                return;
        }
    }
}

