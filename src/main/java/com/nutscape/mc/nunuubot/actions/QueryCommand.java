package com.nutscape.mc.nunuubot.actions;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.function.Predicate;

import com.nutscape.mc.nunuubot.IncomingMessage;

class QueryCommand implements Command
{
    protected ActionPattern comnd;
    protected ActionPattern reply;

    public QueryCommand(String cmdPref,String word,Pattern replyPat,
            Action ca,Action ra) {

        /* Nick comparison is case insensitive in IRC. */
        Map<String,String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        CommandFactory fac = new CommandFactory();
        fac.setCmdPrefix(cmdPref);
        this.comnd = fac.newUserQueryActionPattern(word,map,ca);
        this.reply = fac.newReplyActionPattern(replyPat,map,ra);

        // Check if this user was queried before.
        reply.setPredicate(new Predicate<IncomingMessage>() {
            @Override
            public boolean test(IncomingMessage m) {
                return map.containsKey(m.getNick());
            }
        });
    }

    @Override
    public boolean accept(IncomingMessage m){
        if (m.getCommand().equals("PRIVMSG"))
            return comnd.accept(m);
        return reply.accept(m);
    }

    // Action for sending the query.
    static class QueryAction extends Action {
        protected Map<String,String> map;

        public QueryAction(Map<String,String> map,Action action) {
            super(action);
            this.map = map;
        }

        @Override
        public void doAction(IncomingMessage m,String... args) {
            String target = args[0];
            String channel = m.getDestination();
            map.put(target,channel);
            nextAction.doAction(m,args[0]);
        }
    }

    // Action for receiving the reply to the query.
    static class ReplyAction extends Action {
        protected Map<String,String> map;

        public ReplyAction(Map<String,String> map,Action action) {
            super(action);
            this.map = map;
        }

        @Override
        public void doAction(IncomingMessage m,String... args) {
            String channel = map.remove(m.getNick());
            nextAction.doAction(m,channel);
        }
    }
}

