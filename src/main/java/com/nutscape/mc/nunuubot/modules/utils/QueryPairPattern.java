package com.nutscape.mc.nunuubot.modules.utils;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.nutscape.mc.nunuubot.IRC;

public class QueryPairPattern implements CommandPattern, ReplyPattern {
    private static final int INIT_MAP_SIZE = 5;

    protected ActionPattern comnd;
    protected ActionPattern reply;
    protected Map<String,String> map;

    public QueryPairPattern(String cmdPref,String word,Pattern replyPat,
            Action ca,Action ra) {
        String commPat = cmdPref + word + "( +.*)?";
        Map<String,String> map = new HashMap(INIT_MAP_SIZE);

        Action commandAction = new UserAction(cmdPref,new QueryAction(map,ca));
        Action replyAction = new ReplyAction(map,ra);

        this.map = map;
        this.comnd = new ActionPattern(commPat,commandAction);
        this.reply = new ActionPattern(replyPat,replyAction);
    }

    @Override
    public boolean acceptCommand(String prefix,String dest,String msg,long t){
        return comnd.acceptCommand(prefix,dest,msg,t);
    }

    @Override
    public boolean acceptReply(String prefix,String dest,String msg,long t){
        if (!map.containsKey(IRC.getNick(prefix).toLowerCase())) {
            return false;
        }
        return reply.acceptCommand(prefix,dest,msg,t);
    }
}


