package com.nutscape.mc.nunuubot.modules.utils;

import com.nutscape.mc.nunuubot.IncomingMessage;

/* Action of the type: .cmd[ target] */
public class UserAction extends Action {
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

