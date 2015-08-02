package com.nutscape.mc.nunuubot.modules.utils;

import java.util.Map;

import com.nutscape.mc.nunuubot.IRC;

public class ReplyAction extends Action {
    protected Map<String,String> map;

    public ReplyAction(Map<String,String> map,Action action) {
        super(action);
        this.map = map;
    }

    public void doAction(String target,String dest,String msg,long t) {
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

