package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.CTCP;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.ModuleConfig;
import com.nutscape.mc.nunuubot.NoticeReceiver;
import com.nutscape.mc.nunuubot.modules.utils.ActionPattern;
import com.nutscape.mc.nunuubot.modules.utils.Action;
import com.nutscape.mc.nunuubot.modules.utils.QueryAction;
import com.nutscape.mc.nunuubot.modules.utils.ReplyAction;

public class UtilsModule extends Module implements NoticeReceiver
{
    private Map<String,String> CHANNELS = new HashMap<>();
    private CTCP ctcp;

    private String cmdPrefix =
        "^((" + config.getNickname() + "[:,-] +)|" + config.getSpecialChar() + ")";

    private ActionPattern pingCommand = new ActionPattern(
            cmdPrefix + "ping" + "( +.*)?",new UserAction(cmdPrefix,
                new QueryAction(CHANNELS,new PingAction())));

    private ActionPattern pingReply = new ActionPattern(
            CTCP.Query.PING.replyPattern,
            new ReplyAction(CHANNELS,new PingReplyAction()));

    private ActionPattern versionCommand = new ActionPattern(
            cmdPrefix + "version" + "( +.*)?",new UserAction(cmdPrefix,
                new QueryAction(CHANNELS,new VersionAction())));

    private ActionPattern versionReply = new ActionPattern(
            CTCP.Query.VERSION.replyPattern,
            new ReplyAction(CHANNELS,new VersionReplyAction()));

    public UtilsModule(IRC irc,ModuleConfig config) {
        super(irc,config);
        this.ctcp = new CTCP(irc);
    }

    // ---------

    class PingAction extends Action {
        public void doAction(String target,String dest,String msg,long t) {
            Long timestamp = System.currentTimeMillis();
            ctcp.query(CTCP.Query.PING,target,timestamp.toString());
        }
    }

    class PingReplyAction extends Action {
        public void doAction(String nick,String channel,String msg,long t) {
            // TODO: move
            String arg = CTCP.getArgs(CTCP.Query.PING,msg);
            Long sentTime = Long.valueOf(arg);
            Long delta = t - sentTime;
            irc.sendPrivMessage(channel,nick + ": " + delta + "ms");
        }
    }

    class VersionAction extends Action {
        public void doAction(String target,String dest,String msg,long t) {
            ctcp.query(CTCP.Query.VERSION,target);
        }
    }

    class VersionReplyAction extends Action {
        public void doAction(String nick,String channel,String msg,long t) {
            String arg = CTCP.getArgs(CTCP.Query.PING,msg);
            irc.sendPrivMessage(channel,nick + ": " + arg);
        }
    }

    @Override
    public void privMsg(String prefix,String dest,String msg,long t) {
        if (pingCommand.accept(prefix,dest,msg,t)) {
            return;
        }
        if (versionCommand.accept(prefix,dest,msg,t)) {
            return;
        }
        // add other things ...
    }

    @Override
    public void notice(String prefix,String dest,String msg,long t) {
        if (pingReply.accept(prefix,dest,msg,t)) {
            return;
        }
        if (versionReply.accept(prefix,dest,msg,t)) {
            return;
        }
        // add other things ...
    }
}

