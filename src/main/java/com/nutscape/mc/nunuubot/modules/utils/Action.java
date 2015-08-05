package com.nutscape.mc.nunuubot.modules.utils;

import com.nutscape.mc.nunuubot.IncomingMessage;

public abstract class Action {
    protected Action nextAction;

    protected Action(Action nextAction) {
        this.nextAction = nextAction;
    }

    protected Action() { }

    // Does this need to be public?
    public abstract void doAction(IncomingMessage m,String... args);
}
