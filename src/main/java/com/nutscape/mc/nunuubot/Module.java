package com.nutscape.mc.nunuubot;

import java.util.regex.Pattern;

import com.nutscape.mc.nunuubot.IncomingMessage;

/** 
 * Abstract class for bots.
 * TODO: make assynchronous.
 */
public abstract class Module
{
    protected BotInterface bot;
    protected IRC irc;    // Output interface.

    public Module(IRC irc,BotInterface bot) {
        this.bot = bot;
        this.irc = irc;
    }

    // ------------------

    public void finish() {
        // Do nothing
    }

    public abstract void privMsg(IncomingMessage msg);

    protected boolean match(Pattern p,String s) {
        return p.matcher(s).matches();
    }
}
