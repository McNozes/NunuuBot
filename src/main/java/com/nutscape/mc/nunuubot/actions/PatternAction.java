package com.nutscape.mc.nunuubot.actions;

import com.nutscape.mc.nunuubot.IncomingMessage;
import java.util.regex.Pattern;

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
        if (pattern.matcher(m.getContent()).matches()) {
            // TODO: add log event
            return nextAction.accept(m,args);
        }
        return false;
    }
}
