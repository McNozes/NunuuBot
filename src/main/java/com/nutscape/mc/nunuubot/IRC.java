package com.nutscape.mc.nunuubot;

import java.io.IOException;

/** "Interface" bots use to communicate with the relay.
 *   Offers IRC services to clients. Tries to look like a normal IRC client,
 *   so that it can be easily used by module programmers.
 *
 *   TODO: Insert PRIVMSG 'cooldown' for anti-spam.
 */
public class IRC
{
    private Connection connection;

    public IRC(Connection connection) {
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
        send("USER " + nick + " " + mode +  " * :" + realname);
    }

    public void sendNick(String nick) {
        send("NICK " + nick);
    }

    public void whois(String target) {
        send("WHOIS " + target);
    }

    public void join(String channel) {
        send("JOIN " + channel);
    }

    public void part(String channel) {
        send("PART " + channel);
    }

    public void setMode(char m) {
        send("MODE " + "+" + m);
    }

    public void unsetMode(char m) {
        send("MODE " + "-" + m);    
    }

    public void recievePong(String subject) {
    }

    public void pong(String target) {
        send("PONG " + target);    
    }

    public void ping(String target) {
        send("PING " + target);    
    }

    public void sendPrivMessage(String dest,String msg) {
        send("PRIVMSG " + getNick(dest) + " :" + msg);
    }

    public void sendNotice(String dest,String msg) {
        send("NOTICE " + getNick(dest) + " :" + msg);
    }

    public void nickservIdentify(String password) {
        sendPrivMessage("Nickserv","identify " + password);
    }

    public static String[] decomp(String host) {
        return host.split("!|@",3);
    }

    // TODO: change to getNickname
    public static String getNick(String prefix) {
        return prefix.replaceAll("!.*$","");
    }

    public static String getHost(String prefix) {
        return prefix.replaceAll(".*@","");
    }
}
