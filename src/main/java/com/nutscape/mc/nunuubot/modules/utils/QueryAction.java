package com.nutscape.mc.nunuubot.modules.utils;

import java.util.Map;

import com.nutscape.mc.nunuubot.IncomingMessage;

public class QueryAction extends Action {
    protected Map<String,String> map;

    public QueryAction(Map<String,String> map,Action action) {
        super(action);
        this.map = map;
    }

    @Override
    public void doAction(IncomingMessage m,String... args) {
        String channel = m.getDestination();
        map.put(args[0],channel);
        nextAction.doAction(m,args[0]);
    }
}
