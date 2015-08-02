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

public class UtilsModule extends Module implements NoticeReceiver
{
    private Map<String,String> CHANNELS = new HashMap<>();
    private CTCP ctcp;

    private String cmdPrefix =
        "^((" + config.getNickname() + "[:,-] +)|" + config.getSpecialChar() + ")";

    private PatternAction pingCommand = new PatternAction(
            cmdPrefix + "ping" + "( +.*)?",new UserAction(cmdPrefix,
                new QueryAction(CHANNELS,new PingAction())));

    private PatternAction pingReply = new PatternAction(
            CTCP.Query.PING.replyPattern,
            new QueryReplyAction(CHANNELS,new PingReplyAction()));

    private PatternAction versionCommand = new PatternAction(
            cmdPrefix + "version" + "( +.*)?",new UserAction(cmdPrefix,
                new QueryAction(CHANNELS,new VersionAction())));

    private PatternAction versionReply = new PatternAction(
            CTCP.Query.VERSION.replyPattern,
            new QueryReplyAction(CHANNELS,new VersionReplyAction()));

    public UtilsModule(IRC irc,ModuleConfig config) {
        super(irc,config);
        this.ctcp = new CTCP(irc);
    }

    // ---------

    static abstract class Action {
        protected Action nextAction;

        Action(Action nextAction) {
            this.nextAction = nextAction;
        }

        Action() { }

        // ---------------

        abstract void doAction(String prefix,String dest,String msg,long t);
    }

    /* Class for doing an action when a pattern is matched 
     * TODO: change name of PatternAction */
    static class PatternAction {
        protected Pattern pattern;
        protected Action action;

        public PatternAction(String pattern,Action action) {
            this.pattern = Pattern.compile(pattern);
            this.action = action;
        }
        public PatternAction(Pattern pattern,Action action) {
            this.pattern = pattern;
            this.action = action;
        }
        // ---------------

        public boolean accept(String prefix,String dest,String msg,long t) {
            if (pattern.matcher(msg).matches()) {
                action.doAction(prefix,dest,msg,t);
                return true;
            }
            return false;
        }
    }

    /* Action of the type: .cmd[ target] */
    static class UserAction extends Action {
        protected String cmdPrefix;

        UserAction(String cmdPrefix,Action action) {
            super(action);
            this.cmdPrefix = cmdPrefix;
        }

        void doAction(String prefix,String dest,String msg,long t) {
            // Figure out who is being queried.
            String target = null;
            String[] parts = msg.replaceAll(cmdPrefix,"").split(" +");
            if (parts.length == 2) {
                target = parts[1];
            } else {
                target = prefix;
            }
            nextAction.doAction(target,dest,msg,t);
        }
    }

    static class QueryAction extends Action {
        protected Map<String,String> map;

        QueryAction(Map<String,String> map,Action action) {
            super(action);
            this.map = map;
        }

        void doAction(String target,String dest,String msg,long t) {
            map.put(target.toLowerCase(),dest); // note: lowercase
            nextAction.doAction(target,dest,msg,t);
        }
    }

    static class QueryReplyAction extends Action {
        protected Map<String,String> map;

        QueryReplyAction(Map<String,String> map,Action action) {
            super(action);
            this.map = map;
        }

        void doAction(String target,String dest,String msg,long t) {
            String nick = IRC.getNick(target);
            String channel = map.get(nick.toLowerCase());
            if (channel == null) {
                System.err.println("Error in query: nick not found");
                return;
            }
            nextAction.doAction(nick,channel,msg,t);
            map.remove(nick);
        }
    }


    class PingAction extends Action {
        void doAction(String target,String dest,String msg,long t) {
            Long timestamp = System.currentTimeMillis();
            ctcp.query(CTCP.Query.PING,target,timestamp.toString());
        }
    }

    class PingReplyAction extends Action {
        void doAction(String nick,String channel,String msg,long t) {
            // TODO: move
            String arg = CTCP.getArgs(CTCP.Query.PING,msg);
            Long sentTime = Long.valueOf(arg);
            Long delta = t - sentTime;
            irc.sendPrivMessage(channel,nick + ": " + delta + "ms");
        }
    }

    class VersionAction extends Action {
        void doAction(String target,String dest,String msg,long t) {
            ctcp.query(CTCP.Query.VERSION,target);
        }
    }

    class VersionReplyAction extends Action {
        void doAction(String nick,String channel,String msg,long t) {
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
        System.out.println("a");
        if (pingReply.accept(prefix,dest,msg,t)) {
            return;
        }
        System.out.println("b");
        if (versionReply.accept(prefix,dest,msg,t)) {
            return;
        }
        System.out.println("c");
        // add other things ...
    }
}

