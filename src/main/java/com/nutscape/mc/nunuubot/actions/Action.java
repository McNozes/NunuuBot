package com.nutscape.mc.nunuubot.actions;

import com.nutscape.mc.nunuubot.IncomingMessage;

public abstract class Action {
    protected Action nextAction;

    protected Action(Action nextAction) {
        this.nextAction = nextAction;
    }

    protected Action() { }

    public abstract boolean accept(IncomingMessage m,String... args);
}
