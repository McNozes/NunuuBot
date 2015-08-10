package com.nutscape.mc.nunuubot;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/** "Interface" bots use to communicate with the relay.
 *   Offers IRC services to clients. Tries to look like a normal IRC client,
 *   so that it can be used by module programmers with ease.
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

    public void pong(String target) {
        send("PONG " + target);    
    }

    public void ping(String target) {
        send("PING " + target);    
    }

    public void sendPrivMessage(String dest,String msg) {
        send("PRIVMSG " + dest + " :" + msg);
    }

    public void sendNotice(String dest,String msg) {
        send("NOTICE " + dest + " :" + msg);
    }

    public void quit(String msg) {
        send("QUIT :" + msg);
    }

    public void nickservIdentify(String password) {
        sendPrivMessage("Nickserv","identify " + password);
    }
}
