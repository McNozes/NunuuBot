package com.nutscape.mc.nunuubot.modules.utils;

import java.util.Map;

import com.nutscape.mc.nunuubot.IncomingMessage;

public class ReplyAction extends Action {
    protected Map<String,String> map;

    public ReplyAction(Map<String,String> map,Action action) {
        super(action);
        this.map = map;
    }

    @Override
    public void doAction(IncomingMessage m,String... args) {
        String channel = map.get(m.getNick());
        nextAction.doAction(m,channel);
        map.remove(m.getNick());
    }
}

