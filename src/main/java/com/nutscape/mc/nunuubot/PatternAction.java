package com.nutscape.mc.nunuubot.actions;

import java.util.regex.Pattern;
import java.util.function.Predicate;

import com.nutscape.mc.nunuubot.IncomingMessage;

/* Do an action when a pattern is matched */
class PatternAction extends Action {
    protected Pattern pattern;

    public PatternAction(String pattern,Action action) {
        super(action);
        this.pattern = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
    }

    public PatternAction(Pattern pattern,Action action) {
        super(action);
        this.pattern = pattern;
    }

    @Override
    public boolean accept(IncomingMessage m,String... args) {
        // DEBUG_BEGIN
        System.out.println("Debug: " + pattern);
        // DEBUG_END
        if (pattern.matcher(m.getContent()).matches()) {
            return nextAction.accept(m,args);
        }
        return false;
    }
}
