package com.nutscape.mc.nunuubot.modules.utils;

import java.util.Map;

public class QueryAction extends Action {
    protected Map<String,String> map;

    public QueryAction(Map<String,String> map,Action action) {
        super(action);
        this.map = map;
    }

    @Override
    public void doAction(String target,String dest,String msg,long t) {
        map.put(target.toLowerCase(),dest); // note: lowercase
        nextAction.doAction(target,dest,msg,t);
    }
}
