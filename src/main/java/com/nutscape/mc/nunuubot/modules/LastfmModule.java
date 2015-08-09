package com.nutscape.mc.nunuubot.modules;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.BotInterface;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.CommandFactory;
import com.nutscape.mc.nunuubot.actions.CommandContainer;

public class LastfmModule extends Module
{
    private final String API_KEY = "9088f3a19564351cc470a8f1d3f25745";
    private final String API_SECRET = "5452417c96dd8e6f56d9745c5774ac09";
    private final String API_URL = "http://ws.audioscrobbler.com/2.0/";
    private final String MAP_FILE = "lastfm_users.json";

    private final CommandContainer commands = new CommandContainer();

    private Map<String,String> userMap = 
        new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public LastfmModule(IRC irc,BotInterface bot) {
        super(irc,bot);

        loadMap();

        CommandFactory fac = new CommandFactory();
        fac.setCmdPrefix(bot.getCmdPrefix());
        fac.setIRC(irc);

        commands.add(fac.newMappedCommand("lfmuser",userMap,userInfo));
        commands.add(fac.newMapPutCommand("lfmset",userMap));
    }
    // -----------

    @Override
    public void privMsg(IncomingMessage m) {
        commands.acceptAndReturnAtMatch(m);
    }

    private Action userInfo = new Action() {
        @Override
        public void doAction(IncomingMessage m,String... args) {
            saveMap();
            try {
                String nick = args[0];
                String target = args[1];

                StringBuilder request = requestURL("user.getInfo");
                request.append("&user=" + target);
                URL url = new URL(request.toString());
                HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() != 200) {
                    System.err.println("error response code");
                }

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    irc.sendPrivMessage(m.getDestination(),line);
                }
                in.close();

                conn.disconnect();
            } catch (IOException e) {
                bot.logThrowable(e);
            }
        }
    };

    private StringBuilder requestURL(String methodName) {
        StringBuilder builder = new StringBuilder();
        builder.append(API_URL)
            .append("?method=").append(methodName)
            .append("&format=json")
            .append("&api_key=").append(API_KEY);
        return builder;
    }

    private void response() {
        /* Attributes are expressed as string member values with the attribute
         * name as key.
         * Element child nodes are expressed as object members values with the
         * node name as key.
         * Text child nodes are expressed as string values, unless the element
         * also contains attributes, in which case the text node is expressed
         * as a string member value with the key #text.
         * Repeated child nodes will be grouped as an array member with the
         * shared node name as key. */
    }

    class UserInfo {
        private String name;
        private String realname;
    }

    private void loadMap() {
        Path mapFile = Paths.get(MAP_FILE);
        if (Files.notExists(mapFile)) {
            return;
        }
        bot.log(Level.FINEST,"LastfmModule: Loading user file " + MAP_FILE);

        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

        try {
            Reader in = Files.newBufferedReader(mapFile);
            this.userMap = gson.fromJson(in,this.userMap.getClass());
        } catch (IOException e) {
            bot.logThrowable(e);
        }
    }

    private void saveMap() {
        bot.log(Level.FINEST,"LastfmModule: Saving user file " + MAP_FILE);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(userMap);

        try (Writer out = Files.newBufferedWriter(Paths.get(MAP_FILE))) {
            for (int i=0; i < json.length(); i++) {
                out.write(json.codePointAt(i));
            }
            if (out != null)
                out.close();
        } catch (IOException e) {
            bot.logThrowable(e);
        }
    }

    @Override
    public void finish() {
        saveMap();
    }
}

