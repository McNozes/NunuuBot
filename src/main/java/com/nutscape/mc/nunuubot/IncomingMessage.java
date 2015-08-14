package com.nutscape.mc.nunuubot;

import java.util.regex.Pattern;

/**
 * Contains an IRC message parsed into its basic components, bundled with a
 * timestamp of the approximate time at which it was received.
 *
 * Immutable class (note: it's important that this be immutable).
 */
public class IncomingMessage {
    private String prefix = null;
    private String nick = null;
    private String user = null;
    private String host = null;
    private String command;
    private String arguments = null;
    private String destination = null;
    private String content = null;
    private long timestamp;

    /* Pattern for user's prefixes (as opposed to server's prefixes) */
    private final static Pattern userPrefixRegex =
        Pattern.compile("[^!]+!([^@]+@.*)?");

    public IncomingMessage(String line,long timestamp) {
        this.timestamp = timestamp;

        /* Each line is composed of an optional prefix, a command, and
         * arguments.  
         * If the line starts with a ':', then the first 'word' is a
         * prefix */
        if (line.charAt(0) == ':') {
            String[] cmds = line.split(" +",3);
            this.prefix = stripColon(cmds[0]);
            this.command = cmds[1];
            this.arguments = cmds[2];

            /* Some prefixes are just the name of the server */
            if (userPrefixRegex.matcher(prefix).matches()) {
                String[] prefixComponents = prefix.split("!|@",3);
                this.nick = prefixComponents[0];
                this.user = prefixComponents[1];
                /* The 'host' part is optional */
                if (prefixComponents.length > 2)
                    this.host = prefixComponents[2];
            }
        } else {
            String[] cmds = line.split(" +",2);
            this.command = cmds[0];
            this.arguments = cmds[1];
        }

        if (command.equals("NOTICE")  || command.equals("PRIVMSG")) {
            String[] parts = arguments.split(" ",2);
            this.destination = parts[0];
            this.content = stripColon(parts[1]);
        }
    }

    private static String stripColon(String s) {
        return s.substring(1);
    }

    public String getPrefix()      { return prefix; }
    public String getNick()        { return nick; }
    public String getUser()        { return user; }
    public String getHost()        { return host; }
    public String getCommand()     { return command; }
    public String getArguments()   { return arguments; }
    public String getDestination() { return destination; }
    public String getContent()     { return content; }
    public long getTimestamp()     { return timestamp; }
}
