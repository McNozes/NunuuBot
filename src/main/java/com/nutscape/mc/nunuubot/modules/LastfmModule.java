package com.nutscape.mc.nunuubot.modules;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ConnectException ;
import java.io.IOException;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.BotInterface;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.CommandFactory;
import com.nutscape.mc.nunuubot.actions.CommandContainer;

/* 
 * From LastFM's API documentation, on conversion from XML to JSON:
 * Attributes are expressed as string member values with the attribute
 * name as key.
 * Element child nodes are expressed as object members values with the
 * node name as key.
 * Text child nodes are expressed as string values, unless the element
 * also contains attributes, in which case the text node is expressed
 * as a string member value with the key #text.
 * Repeated child nodes will be grouped as an array member with the
 * shared node name as key.
 */
public class LastfmModule extends Module
{
    private final String API_KEY = "9088f3a19564351cc470a8f1d3f25745";
    private final String API_SECRET = "5452417c96dd8e6f56d9745c5774ac09";
    private final String API_URL = "http://ws.audioscrobbler.com/2.0/";
    private final String MAP_FILE = "lastfm_users.json";

    private final CommandContainer commands = new CommandContainer();

    private Map<String,String> userMap = 
        new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private int newUsers = 0;
    private static final int SAVE_USERS_INTERVAL = 3;

    public LastfmModule(IRC irc,BotInterface bot) {
        super(irc,bot);

        loadMap();

        CommandFactory fac = new CommandFactory();
        fac.setCmdPrefix(bot.getCmdPrefix());
        fac.setIRC(irc);

        String pf = "((lf)|(lfm)|(fm))";
        commands.add(fac.newMappedCommand(pf+"count",userMap,playCountAction));
        commands.add(fac.newMappedCommand(pf + "?np",userMap,nowPlaying));
        commands.add(fac.newMapPutCommand(pf+"set",userMap,saveMap));
    }
    // -----------

    private Action playCountAction = new Action() {
        @Override
        public void doAction(IncomingMessage m,String... args) {
            try {
                String nick = args[0];
                String target = args[1];
                URL url = formBaseRequestURL("user.getInfo","&user=" + target);
                JsonObject resp = makeJsonRequest(url);
                if (resp.has("error")) {
                    handleResponseError(resp,m);
                    return;
                }
                JsonObject userObject = resp.get("user").getAsJsonObject();
                String user = userObject.get("name").getAsString();
                String count = userObject.get("playcount").getAsString();
                String registered = userObject.get("registered").getAsJsonObject()
                    .get("#text").getAsString()
                    .replaceAll(" \\d\\d:\\d\\d$",".");
                irc.sendPrivMessage(m.getDestination(),
                        user + ": " + count + " tracks played since " + registered);
            } catch (IOException e) {
                bot.logThrowable(e);
            }
        }
    };

    private Action nowPlaying = new Action() {
        @Override 
        public void doAction(IncomingMessage m,String... args) {
            try {
                String nick = args[0];
                String target = args[1];
                URL url = formBaseRequestURL("user.getRecentTracks",
                        "&user="+target + "&limit=1");

                JsonObject response = makeJsonRequest(url);
                if (response.has("error")) {
                    handleResponseError(response,m);
                    return;
                }

                JsonObject track = getObject(
                        getObject(response,"recenttracks"),"track");
                bot.log(Level.INFO,track.toString());

                String artist = getString(getObject(track,"artist"),"#text");
                String name = getString(track,"name");

                irc.sendPrivMessage(m.getDestination(),
                        target + " last played " + name + " by " + artist);
            } catch (IOException e) {
                bot.logThrowable(e);
            }

        }
    };

    private JsonObject getObject(JsonObject o,String key) {
        return o.get(key).getAsJsonObject();
    }

    private String getString(JsonObject o,String key) {
        return o.get(key).getAsString();
    }

    private void handleResponseError(JsonObject o,IncomingMessage m) {
        String error = o.get("error").getAsString();
        String message = o.get("message").getAsString();;
        bot.log(Level.WARNING,error + ": " + message);
        if (error.equals("7")) {
            irc.sendPrivMessage(m.getDestination(),"Invalid request.");
        }
    }

    private JsonObject makeJsonRequest(URL url) throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn.getResponseCode() != 200) {
            System.err.println("error response code");
            throw new IOException("Response: " + conn.getResponseCode() +
                    ": "  + conn.getResponseMessage());
        }
        Reader in = new InputStreamReader(conn.getInputStream());
        JsonElement resp = new JsonParser().parse(in);
        conn.disconnect();

        if (!resp.isJsonObject()) {
            throw new JsonParseException("Expected a JSON object.");
        }
        return resp.getAsJsonObject();
    }

    private URL formBaseRequestURL(String methodName,String args) 
        throws MalformedURLException
    {
        StringBuilder builder = new StringBuilder();
        builder.append(API_URL)
            .append("?method=").append(methodName)
            .append("&format=json")
            .append("&api_key=").append(API_KEY)
            .append(args);
        return new URL(builder.toString());
    }

    private Action saveMap = new Action() {
        @Override public void doAction(IncomingMessage m,String...args) {
            newUsers++;
            if (SAVE_USERS_INTERVAL == newUsers) {
                newUsers = 0;
                saveMap();
            }
        }
    };


    class UserInfo {
        private String name;
        private String realname;
    }

    private void loadMap() {
        Path mapFile = Paths.get(MAP_FILE);
        if (Files.notExists(mapFile)) {
            return;
        }
        bot.log(Level.INFO,"LastfmModule: Loading user file " + MAP_FILE);

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

    // TODO: save after 5 new users
    private void saveMap() {
        bot.log(Level.INFO,"LastfmModule: Saving user file " + MAP_FILE);
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
    public void privMsg(IncomingMessage m) {
        commands.acceptAndReturnAtMatch(m);
    }

    @Override
    public void finish() {
        saveMap();
    }
}

