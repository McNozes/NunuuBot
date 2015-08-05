package com.nutscape.mc.nunuubot;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Connection;


public class IRCTests
{
    String[] testLines = new String[] {
        ":NickServ!service@rizon.net NOTICE NunuuBot :This nickname is registered and protected. If it is your"
    };

    @Test
    public void ircTest() {
        IncomingMessage m = new IncomingMessage(testLines[0],1);
        assertEquals(m.getCommand(),"NOTICE");
        assertEquals(m.getNick(),"NickServ");
        assertEquals(m.getUser(),"service");
        assertEquals(m.getHost(),"rizon.net");
        assertEquals(m.getDestination(),"NunuuBot");
        assertEquals(m.getContent(),"This nickname is registered and protected. If it is your");
    }
}
