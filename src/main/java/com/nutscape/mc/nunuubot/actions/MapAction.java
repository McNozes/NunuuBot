package com.nutscape.mc.nunuubot.actions;
import java.util.Map;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;

class MapGetAction extends Action {
    private Map<String,String> map;
    private IRC irc;

    public MapGetAction(IRC irc,Map<String,String> map,Action action) {
        super(action);
        this.map = map;
        this.irc = irc;
    }

    // '@' skips the mapping action
    @Override
    public boolean accept(IncomingMessage m,String... args) {
        String key = args[0];
        String target = (key.charAt(0) == '@') ?
            key.substring(1) : map.get(key);
        if (target == null) {               // not found
            irc.sendPrivMessage(m.getDestination(), key + ": user not found");
            return true;
        } else {
            return nextAction.accept(m,key,target);
        }
    }
}

class MapPutAction extends Action {
    private Map<String,String> map;
    private IRC irc;

    public MapPutAction(IRC irc,Map<String,String> map,Action action) {
        super(action);
        this.map = map;
        this.irc = irc;
    }

    @Override
    public boolean accept(IncomingMessage m,String... args) {
        String username = args[0];
        map.put(m.getNick(),username);
        irc.sendPrivMessage(m.getNick(),"User " + username + " set");
        return nextAction.accept(m,username);
    }
}
