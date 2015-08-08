package com.nutscape.mc.nunuubot.actions;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.function.Predicate;
import java.util.function.Consumer;

import com.nutscape.mc.nunuubot.IncomingMessage;

/* Class for doing an action when a pattern is matched */
class ActionPattern implements Command {
    protected Pattern pattern;
    protected Action action;

    protected Predicate<IncomingMessage> predicate =
        new Predicate<IncomingMessage>() {
        @Override
        public boolean test(IncomingMessage m) {
            return true;
        }
    };

    public ActionPattern(String pattern,Action action) {
        this.pattern = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
        this.action = action;
    }

    public ActionPattern(Pattern pattern,Action action) {
        this.pattern = pattern;
        this.action = action;
    }

    public void setPredicate(Predicate<IncomingMessage> predicate) {
        this.predicate = predicate;
    }

    // ---------------

    @Override
    public boolean accept(IncomingMessage m) {
        if (pattern.matcher(m.getContent()).matches() && predicate.test(m)) {
            action.doAction(m);
            return true;
        }
        return false;
    }
}

