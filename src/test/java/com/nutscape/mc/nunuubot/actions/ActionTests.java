package com.nutscape.mc.nunuubot.actions;

import com.nutscape.mc.nunuubot.IncomingMessage;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ActionTests {
    String resultString;
    boolean resultBoolean;

    String[] commands = new String[] {
        "NunuuBot: host",
        "NunuuBot: host leg"
    };

    Action concatenateThreeArguments = new Action() {
        @Override
        public boolean accept(IncomingMessage m,String... args) {
            resultString = args[0] + args[1] + args[2];
            return true;
        }
    };

    void concatenateThreeArgumentsTest() {
        resultString = "";
        concatenateThreeArguments.accept(null,
                new String[] { "first", "second", "third" });
        assertEquals(resultString,"firstsecondthird");
    }

    void checkArgsActionTest() {
        resultString = "";
        Action checked = new CheckArgsAction(3,concatenateThreeArguments);
        checked.accept(null,new String[] { "first", "second", "third" });
        assertEquals(resultString,"firstsecondthird");

        resultString = "";
        try {
            checked.accept(null,
                    new String[] { "first","second"});
            fail();
        } catch (IllegalArgumentException e) { }

        resultString = "";
        try {
            checked.accept(null,
                    new String[] { "first","second","third","fourth"});
            fail();
        } catch (IllegalArgumentException e) { }
    }

    void optionalTargetCheckArgsActionTest() {
        resultString = "";
        IncomingMessage m = new IncomingMessage(
                ":third!~McNozes@chico.diogo PRIVMSG #McNozes :NunuuBot: host",
                1);
        Action checked = new OptionalTargetCheckArgsAction(3,
                concatenateThreeArguments);

        checked.accept(m,new String[] { "first","second" });
        assertEquals(resultString,"firstsecondthird");

        resultString = "";
        checked.accept(m,new String[] { "first","second","third"});
        assertEquals(resultString,"firstsecondthird");
    }

    void getCmdArgumentsActionTest() {
        resultString = "";
        Action action = new GetCmdArgumentsAction("prefix ",
                concatenateThreeArguments);
        IncomingMessage m = new IncomingMessage(
                ":McNozes!~McNozes@chico.diogo " +
                "PRIVMSG #McNozes :prefix cmd first second third",1);
        action.accept(m);
        assertEquals(resultString,"firstsecondthird");
    }

    void userCommandTest() {
        resultString = "";
        CommandFactory fac = new CommandFactory("prefix ");
    }

    @Test
    public void testActions() {
        concatenateThreeArgumentsTest();
        checkArgsActionTest();
        optionalTargetCheckArgsActionTest();
        getCmdArgumentsActionTest();
        //userCommandTest();
    }
}
