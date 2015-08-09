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
    public void doAction(IncomingMessage m,String... args) {
        String key = args[0];
        String target = (key.charAt(0) == '@') ?
            key.substring(1) : map.get(key);

        if (target == null) {
            irc.sendPrivMessage(m.getDestination(), key + ": user not found");
        } else {
            nextAction.doAction(m,key,target);
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
    public void doAction(IncomingMessage m,String... args) {
        String target = args[0];
        map.put(m.getNick(),target);
        irc.sendPrivMessage(m.getNick(),"User " + target + " set");
        nextAction.doAction(m,target);
    }
}

/*
class MapActionPattern implements Command
{
    private ActionPattern mapAction;
    private ActionPattern mapPut;
    
    MapActionPattern(
            String prefix,
            String commandWord,
            String setWord,
            IRC irc,
            boolean caseIns,
            Action action)
    {
        // Nick comparison is case insensitive in IRC.
        Map<String,String> map;
        if (caseIns) {
            map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        } else {
            map = new HashMap<>();
        }

        CommandFactory fac = new CommandFactory();
        fac.setCmdPrefix(prefix);

        this.mapAction = fac.newUserCommand(commandWord,new MapGetAction(irc,map,action));

        this.mapPut = fac.newArgCommand(setWord,new MapPutAction(map));
    }

    @Override
    public boolean accept(IncomingMessage m){
        return mapPut.accept(m) || mapAction.accept(m);
    }

    // -------------

}
*/

