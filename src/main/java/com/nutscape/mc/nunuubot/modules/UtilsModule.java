package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.ModuleConfig;
import com.nutscape.mc.nunuubot.Pinger;
import com.nutscape.mc.nunuubot.Notices;

public class UtilsModule extends Module implements Pinger,Notices
{
    private Map<String,Long> pinged = new HashMap<>();
    private Map<String,String> channels = new HashMap<>();;

    //private String cmdPrefix = "(" + config.getNickname() + ": +)|" + 
    //    config.getSpecialChar();
    private String cmdPrefix = "(" + config.getNickname() + ": +)";

    public UtilsModule(IRC irc,ModuleConfig config) {
        super(irc,config);
    }

    // ---------

    abstract class ActionPattern {
        private Pattern pattern;

        public ActionPattern(String p) {
            this.pattern = Pattern.compile(p);
        }

        public void accept(String prefix,String dest,String msg) {
            if (pattern.matcher(msg).matches())
                action(prefix,dest,msg);
        }

        public abstract void action(String prefix,String dest,String msg);
    }

    private ActionPattern ping = new ActionPattern(
            cmdPrefix + "ping( +.*)?") {
        @Override 
        public void action(String prefix,String dest,String msg) {
            // Figure out who is being pinged.
            String target = null;
            String[] parts = msg.split(" +");
            if (parts.length == 3) {
                target = parts[2];
            } else {
                target = prefix;
            }
            // Save in maps.
            channels.put(target,dest);
            pinged.put(target,System.currentTimeMillis());
            irc.ping(target);
        }
    };

    @Override
    public void privMsg(String prefix,String dest,String msg) {
        ping.accept(prefix,dest,msg);
    }

    @Override
    public void getPonged(String prefix) {
        Long time = pinged.get(prefix);
        if (time != null) {

        }
    }

    @Override
    public void getNotice(String prefix,String dest,String msg) {
    }

}

