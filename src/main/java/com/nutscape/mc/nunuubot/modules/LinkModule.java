package com.nutscape.mc.nunuubot.modules;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.logging.Level;

import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.Bot;
import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Bot;
import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.ActionContainer;
import com.nutscape.mc.nunuubot.actions.CommandFactory;
import com.nutscape.mc.nunuubot.HtmlUtils;

public class LinkModule extends Module
{
    public static boolean deactivateWithKnownBots = true;

    /* title tags */
    private static String titleBeginTag = "(<title( +[^>]+=[^>]+)*>)";
    private static String titleEndTag = "(</title>)";

    /* title patterns */
    private static Pattern titleBeginPat =
        Pattern.compile(".*" + titleBeginTag + ".*");
    private static Pattern titleEndPat =
        Pattern.compile(".*" + titleEndTag + ".*");

    /* url patterns */
    private Pattern urlPat2 = Pattern.compile(
            "^\\shttps?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    private Pattern urlPat = Pattern.compile(".*(https?://[^ ]+) *.*");

    public LinkModule(Bot bot) throws ModuleInstantiationException
    {
        super(bot);

        CommandFactory fac = new CommandFactory(bot);
        fac.ifNoOtherBotsAction = deactivateWithKnownBots;

        addPrivMsgAction(fac.newPatternAction(urlPat,postLink));
    }

    // -------------------------

    /* Action: post title */
    private Action postLink = new Action() {
        @Override
        public boolean accept(IncomingMessage m,String... args) {
            String nick = m.getNick();
            if (bot.getKnownBots().contains(nick)) {
                return false;
            }

            try {
                bot.log(Level.FINE,"Fetching title...");
                String url = getPageTitle(m.getContent());
                if (!url.equals("")) {
                    bot.getIRC().sendPrivMessage(m.getDestination(),
                            "Title: " + IRC.Colors.bold(url));
                } 
            } catch (Exception e) {
                bot.logThrowable(e);
            }
            return true;
        }
    };

    /*
     * Extract page title.
     */
    private String getPageTitle(String content) throws IOException
    {
        String title = "";

        BufferedReader in = null;
        try {
            /* Fetch page */
            Matcher matcher = urlPat.matcher(content);
            matcher.matches();
            String urlString = matcher.group(1);
            URL url = new URL(urlString);
            InputStream stream;
            try {
                URLConnection conn = url.openConnection();
                conn.connect();
                stream = conn.getInputStream();
            } catch (IOException e) {
                URLConnection conn = url.openConnection();
                conn.addRequestProperty("User-Agent", 
                        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
                conn.connect();
                stream = conn.getInputStream();
            }
            in = new BufferedReader(new InputStreamReader(stream));

            /* Process page */
            String line;
            boolean inTitle = false;
            while ((line = in.readLine()) != null) {
                boolean addedLine = false;

                Matcher matcherBegin = titleBeginPat.matcher(line);
                if (matcherBegin.matches()) {
                    //title = matcherBegin.group(2);
                    title = line.replaceFirst(".*" + titleBeginTag,"");
                    addedLine = true;
                    inTitle = true;
                } 

                if (inTitle && !addedLine) {
                    title += line;
                    addedLine = true;
                }

                Matcher matcherEnd = titleEndPat.matcher(line);
                if (matcherEnd.matches()) {
                    /*
                    if (!addedLine) {
                        title += line;
                        addedLine = true;
                    }
                    */
                    title = title.replaceFirst(titleEndTag + ".*","");
                    break;
                } 
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return HtmlUtils.substEscape(title);
    }
}
