package com.nutscape.mc.nunuubot.modules.utils;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.nutscape.mc.nunuubot.IncomingMessage;

public class QueryPairPattern implements CommandPattern, ReplyPattern
{
    protected ActionPattern comnd;
    protected ActionPattern reply;
    protected Map<String,String> map;

    public QueryPairPattern(String cmdPref,String word,Pattern replyPat,
            Action ca,Action ra) {

        /* Nick comparison is case insensitive in IRC. */
        this.map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        Action commandAction = new UserAction(cmdPref,new QueryAction(map,ca));
        Action replyAction = new ReplyAction(map,ra);

        String commPattern = cmdPref + word + "( +.*)?";
        this.comnd = new ActionPattern(commPattern,commandAction);
        this.reply = new ActionPattern(replyPat,replyAction);
    }

    @Override
    public boolean acceptCommand(IncomingMessage m){
        return comnd.acceptCommand(m);
    }

    @Override
    public boolean acceptReply(IncomingMessage m){
        if (!map.containsKey(m.getNick())) {
            return false;
        }
        return reply.acceptCommand(m);
    }
}


