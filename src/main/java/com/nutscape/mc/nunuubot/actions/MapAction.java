package com.nutscape.mc.nunuubot.actions;
import java.util.Map;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;

class MapGetAction extends Action {
    private Map<String,String> map;
    private IRC irc;
    private int argIndex;
    private String mapCommandString;

    public MapGetAction(IRC irc,Map<String,String> map,int argIndex,
            String mapCommandString,Action action) {
        super(action);
        this.map = map;
        this.irc = irc;
        this.argIndex = argIndex;
        this.mapCommandString = mapCommandString;
    }

    /*
     * '@' skips the mapping action.
     * The mapped (or raw) 'value' is appended to the argument list.
     */
    @Override
    public boolean accept(IncomingMessage m,String... args) {
        String key = args[argIndex];
        String value = (key.charAt(0) == '@') ?
            key.substring(1) : map.get(key);
        if (value == null) {               // not found
            irc.sendPrivMessage(m.getDestination(), 
                    key + ": not found. Send me a pvt with " + mapCommandString
                    + " <username>");
            return true;
        }
        // Add the value to the end of argument list
        int n = args.length;
        String[] newArgs = new String[n+1];
        newArgs[n] = value;
        System.arraycopy(args,0,newArgs,0,n);
        return nextAction.accept(m,newArgs);
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
        irc.sendNotice(m.getNick(),"User " + username + " set.");
        return nextAction.accept(m,username);
    }
}
