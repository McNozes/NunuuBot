package com.nutscape.mc.nunuubot.modules;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IRC.CTCP;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.Bot;
import com.nutscape.mc.nunuubot.NoticeReceiver;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.ActionContainer;
import com.nutscape.mc.nunuubot.actions.CommandFactory;
import com.nutscape.mc.nunuubot.ChannelUser;


public class TokensModule extends Module {

    public TokensModule(Bot bot) throws ModuleInstantiationException {
        super(bot);

        CommandFactory fac = new CommandFactory(bot);

        addPrivMsgAction(fac.newUserCommand("tokens",postTokensAction));
    }

    private Action postTokensAction = new Action() {
        @Override
        public boolean accept(IncomingMessage m,String... args) {

            String channel = m.getDestination();
            String nick = m.getNick();
            ChannelUser chanUser = bot.getChannelUser(channel,nick);
            if (chanUser == null) {
                return true;
            }

            bot.getIRC().sendPrivMessage(m.getDestination(),
                        nick + ": " + chanUser.getTokens() + " tokens");

            return true;
        }
    };

}
