package com.nutscape.mc.nunuubot.actions;

import java.util.Collection;
import java.util.ArrayList;

import com.nutscape.mc.nunuubot.IncomingMessage;

public class ActionContainer {
    private final Collection<Action> actions = new ArrayList<>();

    public void add(Action c) {
        actions.add(c);
    }

    public int size() {
        return actions.size();
    }

    public void acceptAndReturnAtMatch(IncomingMessage m) {
        for (Action a : actions) {
            if (a.accept(m)) {
                return;
            }
        }
    }
}
