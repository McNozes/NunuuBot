package com.nutscape.mc.nunuubot.actions;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.CommandFactory;
import java.util.Map;
import java.util.regex.Pattern;

public class CommandFactory {
    private String cmdPrefix;
    private IRC irc;

    public CommandFactory(String cmdPrefix) {
        this.cmdPrefix = cmdPrefix;
    }

    public void setIRC(IRC irc) {
        this.irc = irc;
    }

    private Pattern buildCmdPattern(String cmd) {
        StringBuilder builder = new StringBuilder();
        builder.append(cmdPrefix).append(" *").append(cmd).append("( .*)?");
        String p = builder.toString();
        return Pattern.compile(p,Pattern.CASE_INSENSITIVE);
    }

    // --------------

    public Action newCommand(String cmd,Action action,int nargs,
            boolean usesOptionalTarget) {
        Pattern pattern = buildCmdPattern(cmd);
        Action check = usesOptionalTarget ? 
            new OptionalTargetCheckArgsAction(nargs,action) :
            new CheckArgsAction(nargs,action);
        Action getArgs = new GetCmdArgumentsAction(cmdPrefix,check);
        return new PatternAction(pattern,getArgs);
    }

    // Simple commands

    /* Command of type 'cmd arg1, arg2, ..., arg_nargs' */
    public Action newCommand(String cmd,int nargs,Action action) {
        return newCommand(cmd,action,nargs,false);
    }

    /* Command of type 'cmd arg1' */
    public Action newCommand(String cmd,Action action) {
        return newCommand(cmd,1,action);
    }

    // Commands with option user

    /* Command of type 'cmd arg1, ..., arg_nargs [target]' */
    public Action newUserCommand(String cmd,int nargs,Action action) {
        return newCommand(cmd,action,nargs,true);
    }

    /* Command of type 'cmd [target]' */
    public Action newUserCommand(String cmd,Action action) {
        return newUserCommand(cmd,1,action);
    }

    // Queries

    /* Command of type 'query [target]' */
    public Action newQueryCommand(String cmd,Pattern replyPat,
            Action ca,Action ra) {
        return new QueryPairAction(cmdPrefix,cmd,replyPat,ca,ra);
    }

    // Maps

    /* Command of type 'cmd arg ... key ....arg ...' */
    public Action newMappedCommand(String cmd,Map<String,String> map,
            int argIndex,String mapCommandString, Action action) {
        return newUserCommand(cmd,
                new MapGetAction(irc,map,argIndex,mapCommandString,action));
    }

    /* Command of type 'cmd [nick]' */
    public Action newMappedCommand(String cmd,Map<String,String> map,
            String mapCommandString, Action action) {
        return newMappedCommand(cmd,map,0,mapCommandString,action);
    }

    /* Command of type 'cmd nick1 [nick2]' */
    public Action newDoubleMappedCommand(String cmd,Map<String,String> map,
            String mapString, Action action) {
        Action second = new MapGetAction(irc,map,1,mapString,action);
        Action first = new MapGetAction(irc,map,0,mapString,second);
        int nargs = 2;
        return newUserCommand(cmd,nargs,first);
    }

    /* Command of type 'cmd-set value'. */
    public Action newMapPutCommand(String cmd,Map<String,String> map,
            Action action) {
        return newCommand(cmd,new MapPutAction(irc,map,action));
    }

    /* Terminal 'cmd-set value' command. */
    public Action newMapPutCommand(String cmd,Map<String,String> map) {
        return newMapPutCommand(cmd,map,new DoNothingAction());
    }
}

// Do nothing
class DoNothingAction extends Action {
    @Override
    public boolean accept(IncomingMessage m,String...args) { 
        return true;
    }
}

/* Prints error and returns if the number of arguments doesn't match.  */
class CheckArgsAction extends Action {
    protected int nargs;

    CheckArgsAction(int nargs,Action action) {
        super(action);
        this.nargs = nargs;
    }

    @Override
    public boolean accept(IncomingMessage m,String... args) {
        if (args.length != nargs) {
            throw new IllegalArgumentException("Wrong number of arguments in action");
        } 
        return nextAction.accept(m,args);
    }
}

class OptionalTargetCheckArgsAction extends CheckArgsAction {
    OptionalTargetCheckArgsAction(int nargs,Action action) {
        super(nargs,action);
    }

    @Override
    public boolean accept(IncomingMessage m,String... args) {
        if (args.length == nargs-1) {
            // Insert the nick of the sender in the arguments list
            String[] newArgs = new String[args.length+1];
            for (int i=0; i < args.length; i++) {
                newArgs[i] = args[i];
            }
            newArgs[args.length] = m.getNick();
            args = newArgs;
        }
        return super.accept(m,args);
    }
}

// Retrieves arguments from the command (thus, ignores the prefix and the
// first word).
class GetCmdArgumentsAction extends Action {
    protected String cmdPrefix;

    GetCmdArgumentsAction(String cmdPrefix,Action action) {
        super(action);
        this.cmdPrefix = cmdPrefix;
    }

    @Override
    public boolean accept(IncomingMessage m,String... args) {
        String noPrefix = m.getContent().replaceAll(cmdPrefix,"")
            .replaceAll("^ +","");
        String[] parts = noPrefix.split(" ");
        String[] newArgs;
        if (parts.length == 1 && parts[0].equals("")) {
            // Array of length zero:
            newArgs = new String[0];
        } else {
            newArgs= new String[parts.length-1];
            for (int i=1; i < parts.length; i++) {
                newArgs[i-1] = parts[i];
            }
        }
        return nextAction.accept(m,newArgs);
    }
}
