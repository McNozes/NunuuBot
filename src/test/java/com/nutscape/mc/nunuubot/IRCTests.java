package com.nutscape.mc.nunuubot;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.nutscape.mc.nunuubot.IncomingMessage;

public class IRCTests
{
    String[] testLines = new String[] {
    };

    @Test
    public void simpleMessageTest() {
        String message = ":NickServ!service@rizon.net NOTICE NunuuBot :This nickname is registered and protected. If it is your";
        int timestamp = 14123;
        IncomingMessage m1 = new IncomingMessage(message,timestamp);
        assertEquals(m1.getCommand(),"NOTICE");
        assertEquals(m1.getNick(),"NickServ");
        assertEquals(m1.getUser(),"service");
        assertEquals(m1.getHost(),"rizon.net");
        assertEquals(m1.getDestination(),"NunuuBot");
        assertEquals(m1.getTimestamp(),timestamp);
        assertEquals(m1.getContent(),"This nickname is registered and protected. If it is your");
    }

    @Test
    public void hostMessageTest() {
        String message =
            ":McNozes!~McNozes@chico.diogo PRIVMSG #McNozes :NunuuBot: host";
        IncomingMessage m2 = new IncomingMessage(message,12345);
        assertEquals(m2.getCommand(),"PRIVMSG");
        assertEquals(m2.getNick(),"McNozes");
        assertEquals(m2.getUser(),"~McNozes");
        assertEquals(m2.getHost(),"chico.diogo");
        assertEquals(m2.getDestination(),"#McNozes");
        assertEquals(m2.getContent(),"NunuuBot: host");
    }
}
