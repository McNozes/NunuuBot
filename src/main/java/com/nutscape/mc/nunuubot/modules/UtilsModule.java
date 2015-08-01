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
    private CTCP ctcp;

    private Map<String,String> channels = new HashMap<>(5);

    private String cmdPrefix =
        "^((" + config.getNickname() + ": +)|" + config.getSpecialChar() + ")";

    public UtilsModule(IRC irc,ModuleConfig config) {
        super(irc,config);
        this.ctcp = new CTCP(irc);
    }

    // ---------

    abstract class PatternAction {
        private Pattern pattern;

        public PatternAction(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        public PatternAction(Pattern pattern) {
            this.pattern = pattern;
        }

        boolean matches(String msg) {
            return pattern.matcher(msg).matches();
        }

        public boolean accept(String prefix,String dest,String msg,long t) {
            if (pattern.matcher(msg).matches()) {
                action(prefix,dest,msg,t);
                return true;
            }
            return false;
        }

        abstract void action(String prefix,String dest,String msg,long t);
    }

    private PatternAction pingCommand = new PatternAction(
            cmdPrefix + "ping( +.*)?") {
        @Override 
        public void action(String prefix,String dest,String msg,long t) {
            // Figure out who is being pinged.
            String target = null;
            String[] parts = msg.replaceAll(cmdPrefix,"").split(" +");
            if (parts.length == 2) {
                target = parts[1];
            } else {
                target = prefix;
            }
            // Save in maps and do ping.
            channels.put(target.toLowerCase(),dest);
            Long timestamp = System.currentTimeMillis();
            ctcp.query(CTCP.Query.PING,target,timestamp.toString());
        }
    };

    private PatternAction pingReply = new PatternAction(
            CTCP.Query.PING.replyPattern) {
        @Override
        public void action(String prefix,String dest,String msg,long t) {
            String arg = CTCP.getArgs(CTCP.Query.PING,msg);
            Long sentTime = Long.valueOf(arg);
            Long delta = t - sentTime;
            String nick = IRC.getNick(prefix);
            String channel = channels.get(nick.toLowerCase());
            if (channel != null) {
                irc.sendPrivMessage(channel,nick + ": " + delta + "ms");
                channels.remove(nick);
            } else {
                System.err.println("Error in ping: nick not found.");
            }
        }
    };

    @Override
    public void privMsg(String prefix,String dest,String msg,long t) {
        if (pingCommand.accept(prefix,dest,msg,t)) {
            return;
        }
        // add other things ...
    }

    @Override
    public void notice(String prefix,String dest,String msg,long t) {
        if (pingReply.accept(prefix,dest,msg,t)) {
            return;
        }
        // add other things ...
    }
}

