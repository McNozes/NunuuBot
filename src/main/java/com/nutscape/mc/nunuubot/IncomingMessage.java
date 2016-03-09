package com.nutscape.mc.nunuubot;

import java.util.regex.Pattern;

/**
 * Contains an IRC message parsed into its basic components, bundled with a
 * timestamp of the approximate time at which it was received.
 *
 * Immutable class (note: it's important that this be immutable).
 */
public class IncomingMessage {

    /** 'prefix' associated with message. May be a server prefix. */
    public String getPrefix()      { return prefix; }

    /** 'nickname' associated with message. */
    public String getNick()        { return nick; }

    /** 'username' associated with message (not necessarily 'nickname'). */
    public String getUser()        { return user; }

    /** 'hostname' associated with message. */
    public String getHost()        { return host; }

    /** "Type" of message. May be "PRIVMSG", "KICK", "NOTICE", etc */
    public String getCommand()     { return command; }

    /** All the text that comes after "type/command". */
    public String getArguments()   { return arguments; }

    /** Returns 'destination', which is part of some IRC messages (such as
     * PRIVMSG).
     * For example, messages in a chatroom #chat are of the form:
     *
     *   :prefix PRIVMSG #chat :message-content
     *
     * In this case, "#chat" is the destination.
     */
    public String getDestination() { return destination; }

    /** Returns 'content', which is part of some IRC messages (such as
     * PRIVMSG).
     * For example, messages in a chatroom #chat are of the form:
     *
     *   :prefix PRIVMSG #chat :message-content
     *
     * In this case, "message-content" is the content. getContent() strips away
     * the first ':'.
     */
    public String getContent()     { return content; }

    /** Timestamp associated with message. */
    public long getTimestamp()     { return timestamp; }

    // -------------------------

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

        if (
                command.equals("NOTICE") ||
                command.equals("PRIVMSG") || 
                command.equals("KICK") ||
                command.equals("353")) {
            String[] parts = arguments.split(" +",2);
            this.destination = parts[0];
            this.content = stripColon(parts[1]);
        }
    }

    public static String stripColon(String s) {
        return s.charAt(0) == ':' ? s.substring(1) : s;
    }

    public String toString() {
        return 
            "prefix " + prefix + "\n" +
            "nick " + nick + "\n" +
            "user " + user + "\n" +
            "host " + host + "\n" +
            "command " + command + "\n" +
            "arguments " + arguments + "\n" +
            "destination " + destination  + "\n" +
            "content " + content + "\n" +
            "timestamp " + timestamp;
    }

}
