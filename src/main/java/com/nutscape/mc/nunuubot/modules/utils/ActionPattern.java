package com.nutscape.mc.nunuubot.modules.utils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/* Class for doing an action when a pattern is matched */
public class ActionPattern {
    protected Pattern pattern;
    protected Action action;

    public ActionPattern(String pattern,Action action) {
        this.pattern = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
        this.action = action;
    }
    public ActionPattern(Pattern pattern,Action action) {
        this.pattern = pattern;
        this.action = action;
    }
    // ---------------

    public boolean accept(String prefix,String dest,String msg,long t) {
        if (pattern.matcher(msg).matches()) {
            action.doAction(prefix,dest,msg,t);
            return true;
        }
        return false;
    }
}

