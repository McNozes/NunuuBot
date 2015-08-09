package com.nutscape.mc.nunuubot.actions;

import java.util.Map;
import java.util.regex.Pattern;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;

public class CommandFactory {
    private String cmdPrefix = "";
    private IRC irc;

    public void setCmdPrefix(String cmdPrefix) {
        this.cmdPrefix = cmdPrefix;
    }

    public void setIRC(IRC irc) {
        this.irc = irc;
    }

    public ActionPattern newArgCommand(String word,Action action) {
        String pattern = cmdPrefix + word + " +[^ ]+ *";
        return new ActionPattern(pattern,new ArgAction(cmdPrefix,action));
    }

    public ActionPattern newUserCommand(String word,Action action) {
        String pattern = cmdPrefix + word + "( +[^ ]+ *)?";
        return new ActionPattern(pattern,new UserAction(cmdPrefix,action));
    }

    public QueryCommand newQueryCommand(String word,Pattern replyPat,
            Action ca,Action ra) {
        return new QueryCommand(cmdPrefix,word,replyPat,ca,ra);
    }

    public ActionPattern newMappedCommand(String word,Map<String,String> map,
            Action action) {
        return newUserCommand(word,new MapGetAction(irc,map,action));
    }

    public ActionPattern newMapPutCommand(String word,
            Map<String,String> map,Action action) {
        return newArgCommand(word,new MapPutAction(irc,map,action));
    }

    public ActionPattern newMapPutCommand(String word,Map<String,String> map) {
        return newArgCommand(word,
                new MapPutAction(irc,map,new DoNothingAction()));
    }
}
