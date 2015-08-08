package com.nutscape.mc.nunuubot.actions;

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

/* Action of the type: .cmd[ target] */
class UserAction extends Action {
    protected String cmdPrefix;

    public UserAction(String cmdPrefix,Action action) {
        super(action);
        this.cmdPrefix = cmdPrefix;
    }

    @Override
    public void doAction(IncomingMessage m,String... args) {
        // Figure out who is being queried.
        String[] parts = m.getContent().replaceAll(cmdPrefix,"").split(" +");
        String target = (parts.length == 2) ? parts[1] : m.getNick();
        nextAction.doAction(m,target);
    }
}