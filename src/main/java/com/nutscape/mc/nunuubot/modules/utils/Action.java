package com.nutscape.mc.nunuubot.modules.utils;

public abstract class Action {
    protected Action nextAction;

    protected Action(Action nextAction) {
        this.nextAction = nextAction;
    }

    protected Action() { }

    // Does this need to be public?
    public abstract void doAction(String prefix,String dest,String msg,long t);
}
