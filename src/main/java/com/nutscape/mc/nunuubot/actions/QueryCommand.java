package com.nutscape.mc.nunuubot.actions;

import com.nutscape.mc.nunuubot.IncomingMessage;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

class QueryPairAction extends Action
{
    protected Action comnd;
    protected Action reply;

    /* Nick comparison is case insensitive in IRC. */
    protected Map<String,String> map =
        new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public QueryPairAction(String cmdPref,String word,Pattern replyPat,
            Action ca,Action ra) {
        CommandFactory fac = new CommandFactory(cmdPref);
        this.comnd = fac.newUserCommand(word,new QueryAction(ca));
        this.reply = new PatternAction(replyPat,new ReplyAction(ra));
    }

    @Override
    public boolean accept(IncomingMessage m,String...args){
        if (m.getCommand().equals("PRIVMSG")) {
            return comnd.accept(m,args);
        } else {
            return reply.accept(m,args);
        }
    }

    // Action for sending the query.
    class QueryAction extends Action {
        public QueryAction(Action action,String...args) {
            super(action);
        }

        @Override
        public boolean accept(IncomingMessage m,String... args) {
            String target = args[args.length-1];
            String channel = m.getDestination();
            map.put(target,channel);
            return nextAction.accept(m,args[0]);
        }
    }

    // Action for receiving the reply to the query.
    class ReplyAction extends Action {
        public ReplyAction(Action action) {
            super(action);
        }

        @Override
        public boolean accept(IncomingMessage m,String... args) {
            String channel = map.remove(m.getNick());
            if (channel == null) {
                return false;
            }
            return nextAction.accept(m,channel);
        }
    }
}
