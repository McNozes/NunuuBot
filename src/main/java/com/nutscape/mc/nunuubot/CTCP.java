package com.nutscape.mc.nunuubot;

import java.util.regex.Pattern;

/**
 * Client To Client Protocol
 */
public class CTCP {
    /* Supported CTCP queries. */
    public enum Query {
        FINGER,
        VERSION,
        USERINFO,
        CLIENTINFO,
        SOURCE,
        ERRMSG,
        PING,
        TIME;

        /* Pattern matches if a certain message is a valid reply to a 
         * CTCP query. */
        public final Pattern replyPattern = Pattern.compile(
                '\001' + toString() + "( +[^ ].*)?\001",
                Pattern.CASE_INSENSITIVE);
    };

    private IRC irc;

    public CTCP(IRC irc) {
        this.irc = irc;
    }

    // --------------

    // Send CTCP query.
    public void query(Query q,String target,String args) {
        irc.sendPrivMessage(target,
                '\001' + q.toString() + ' ' + args + '\001');
    }

    // Send CTCP query.
    public void query(Query q,String target) {
        irc.sendPrivMessage(target,'\001' + q.toString()+ '\001');
    }

    public static String getArgs(Query q,String msg) {
        return msg.replaceAll("(^\001[^ ]+ +)|(\001$)","");
    }
}
