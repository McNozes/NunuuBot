package com.nutscape.mc.nunuubot.actions;

import java.util.Map;
import java.util.regex.Pattern;

public class CommandFactory {
    private String cmdPrefix = "";

    public ActionPattern newUserCommand(String word,Action action) {
        String pattern = cmdPrefix + word + "( +.*)?";
        return new ActionPattern(pattern,new UserAction(cmdPrefix,action));
    }

    public QueryCommand newQueryCommand(String word,Pattern replyPat,
            Action ca,Action ra) {
        return new QueryCommand(cmdPrefix,word,replyPat,ca,ra);
    }

    ActionPattern newUserQueryActionPattern(
            String word,
            Map<String,String> map,
            Action action) {
        return newUserCommand(word,
                new QueryCommand.QueryAction(map,action));
    }

    ActionPattern newReplyActionPattern(
            Pattern replyPat,
            Map<String,String> map,
            Action action) {
        return new ActionPattern(replyPat,
                new QueryCommand.ReplyAction(map,action));
    }

    public void setCmdPrefix(String cmdPrefix) {
        this.cmdPrefix = cmdPrefix;
    }
}
