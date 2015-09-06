package com.nutscape.mc.nunuubot;
 
import com.nutscape.mc.nunuubot.Connection;
import java.io.IOException;
import java.util.regex.Pattern;

/** "Interface" bots use to communicate with the relay.
 *   Offers IRC services to clients. Tries to look like a normal IRC client,
 *   so that it can be used by module programmers with ease.
 *
 *   TODO: Insert PRIVMSG 'cooldown' for anti-spam.
 */
public class IRC
{
    private Connection connection;
    private Bot bot;

    public IRC(Connection connection,Bot bot) {
        this.connection = connection;
    }

    private void send(String msg) {
        try {
            connection.send(msg);
        } catch (IOException e) {
            System.err.println("send() IOException: " + e.getMessage()); 
        }
    }

    // ---------------------

    public void sendUser(String nick,String mode,String realname) {
        send("USER " + nick + " " + mode +  " * :" + realname); }

    public void sendNick(String nick) {
        send("NICK " + nick); }

    public void whois(String target) {
        send("WHOIS " + target); }

    public void join(String channel) {
        send("JOIN " + channel); }

    public void part(String channel) {
        send("PART " + channel); }

    public void setMode(char m) {
        send("MODE " + "+" + m); }

    public void unsetMode(char m) {
        send("MODE " + "-" + m);    }

    public void pong(String target) {
        send("PONG " + target); }

    public void ping(String target) {
        send("PING " + target);    }

    public void sendPrivMessage(String dest,String msg) {
        send("PRIVMSG " + dest + " :" + msg); }

    public void sendNotice(String dest,String msg) {
        send("NOTICE " + dest + " :" + msg); }

    public void quit(String msg) {
        send("QUIT :" + msg); }

    public void nickservIdentify(String password) {
        sendPrivMessage("Nickserv","identify " + password); }

    /**
     * Client To Client Protocol
     */
    public enum CTCP {
        /* Supported CTCP queries. */
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

        public static String getArgs(String msg) {
            return msg.replaceAll("(^\001[^ ]+ +)|(\001$)","");
        }
    }

    // Send CTCP query.
    public void query(CTCP q,String target,String args) {
        sendPrivMessage(target,'\001' + q.toString() + ' ' + args + '\001');
    }

    // Send CTCP query.
    public void query(CTCP q,String target) {
        sendPrivMessage(target,'\001' + q.toString()+ '\001');
    }

    /*
     * mIRC colors
     * "Text formatting is done via a set of special character sequences that
     * are parsed by the IRC client. Every new text format starts with one of
     * the formatting control character. The same control character can be used
     * at the end of the format in order to terminate it.
     */
    public static class Colors {
        enum CODE {
            C(0x03),
            PLAIN(0x0F),

            BOLD(0x02),
            ITALIC(0x1D),
            UNDERLINE(0x1F),
            REVERSE(0x16),

            WHITE(0x00),
            BLACK(0x01),
            NAVYBLUE (0x02),
            GREEN(0x03),
            RED(0x04),
            BROWN(0x05),
            PURPLE(0x06),
            OLIVE(0x07),
            YELLOW(0x08),
            LIMEGREEN(0x09),
            TEAL(0x0A),
            CYAN(0x0B),
            ROYALBLUE(0x0C),
            HOTPINK(0x0D),
            DARKGRAY(0x0E),
            LIGHTGRAY(0x0F);

            String VALUE;

            CODE(int code) {
                this.VALUE = String.valueOf((char)code);
            }

            public String toString() {
                return VALUE;
            }
        };

        private static String transform(String...args) {
            StringBuilder builder = new StringBuilder();
            for (String a : args) {
                builder.append(a);
            }
            return builder.toString();
        }

        private static String simpleFormat(CODE code,String msg) {
            return transform(code.toString(),msg,code.toString()); }

        // Public methods

        public static String bold(String msg) {
            return simpleFormat(CODE.BOLD,msg); }

        public static String italic(String msg) {
            return simpleFormat(CODE.BOLD,msg); }

        public static String underline(String msg) {
            return simpleFormat(CODE.ITALIC,msg); }

        public static String reverse(String msg) {
            return simpleFormat(CODE.ITALIC,msg); }

        public static String color(CODE forec,CODE backc,String msg) {
            String cString = CODE.C.toString();
            String forecString = forec.toString();
            String backcString = backc.toString();
            return transform(cString,forecString,",",backcString,msg,cString);
        }

        public static String color(CODE color,String msg) {
            String cString = CODE.C.toString();
            String colorString = color.toString();
            return transform(cString,colorString,msg,cString); }

        public static String spoiler(String msg) {
            return color(CODE.BLACK,CODE.BLACK,msg); }

        public static String green(String msg) {
            return color(CODE.GREEN,msg); }
    }
}
