package com.nutscape.mc.nunuubot.actions;

import java.util.Collection;
import java.util.ArrayList;

import com.nutscape.mc.nunuubot.IncomingMessage;

public class CommandContainer {
    private final Collection<Command> commands = new ArrayList<>();

    public void add(Command c) {
        commands.add(c);
    }

    public void acceptAndReturnAtMatch(IncomingMessage m) {
        for (Command command : commands) {
            if (command.accept(m)) {
                return;
            }
        }
    }
}
