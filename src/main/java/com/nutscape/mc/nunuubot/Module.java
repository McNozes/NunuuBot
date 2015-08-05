package com.nutscape.mc.nunuubot;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.nutscape.mc.nunuubot.IncomingMessage;

/** 
 * Abstract class for bots.
 * TODO: make assynchronous.
 */
public abstract class Module
{
    protected ModuleConfig config;
    protected IRC irc;    // Output interface.

    public Module(IRC irc,ModuleConfig config) {
        this.config = config;
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
