package com.nutscape.mc.nunuubot.modules.utils;

/* Action of the type: .cmd[ target] */
public class UserAction extends Action {
    protected String cmdPrefix;

    public UserAction(String cmdPrefix,Action action) {
        super(action);
        this.cmdPrefix = cmdPrefix;
    }

    @Override
    public void doAction(String prefix,String dest,String msg,long t) {
        // Figure out who is being queried.
        String target = null;
        String[] parts = msg.replaceAll(cmdPrefix,"").split(" +");
        if (parts.length == 2) {
            target = parts[1];
        } else {
            target = prefix;
        }
        nextAction.doAction(target,dest,msg,t);
    }
}

