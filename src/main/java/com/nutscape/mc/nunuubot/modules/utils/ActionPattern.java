package com.nutscape.mc.nunuubot.modules.utils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.nutscape.mc.nunuubot.IncomingMessage;

/* Class for doing an action when a pattern is matched */
public class ActionPattern implements CommandPattern {
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

    @Override
    public boolean acceptCommand(IncomingMessage m) {
        if (pattern.matcher(m.getContent()).matches()) {
            action.doAction(m);
            return true;
        }
        return false;
    }
}

