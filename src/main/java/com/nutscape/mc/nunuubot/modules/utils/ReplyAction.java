package com.nutscape.mc.nunuubot.modules.utils;

import java.util.Map;

import com.nutscape.mc.nunuubot.IRC;

public class ReplyAction extends Action {
    protected Map<String,String> map;

    public ReplyAction(Map<String,String> map,Action action) {
        super(action);
        this.map = map;
    }

    @Override
    public void doAction(String target,String dest,String msg,long t) {
        String nickLowerCase = IRC.getNick(target).toLowerCase();
        String channel = map.get(nickLowerCase);
        nextAction.doAction(target,channel,msg,t);
        map.remove(nickLowerCase);
    }
}

