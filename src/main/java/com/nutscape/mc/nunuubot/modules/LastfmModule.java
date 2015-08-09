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
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonArray;

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

                JsonObject resp = makeJsonRequest(url);
                // Check for errors
                if (resp.has("error")) {
                    handleResponseError(resp,m);
                    return;
                }
                StringBuilder msg = new StringBuilder();
                JsonElement el = getObject(resp,"recenttracks").get("track");
                if (!el.isJsonArray()) {
                    JsonObject track = el.getAsJsonObject();
                    msg.append(nick);
                    msg.append(": ");
                    msg.append(getTrackString(track));
                    msg.append(" ");
                    msg.append(getTimeString(track));
                } else {
                    // LastFM returns an array with two tracks when the track
                    // is currently playing.
                    JsonArray array = el.getAsJsonArray();
                    // It's the second element
                    JsonObject track = array.get(1).getAsJsonObject();
                    msg.append(target);
                    msg.append(" is now playing ");
                    msg.append(getTrackString(track));
                }

                //bot.log(Level.INFO,track.toString());
                irc.sendPrivMessage(m.getDestination(),msg.toString());

            } catch (IOException e) {
                bot.logThrowable(e);
            }
        }

        private String getTrackString(JsonObject track)
        {
            StringBuilder builder = new StringBuilder();
            builder.append("\"");
            builder.append(getString(track,"name"));
            builder.append("\" by \"");
            builder.append(getString(getObject(track,"artist"),"#text"));
            builder.append("\" (\"");
            builder.append(getString(getObject(track,"album"),"#text"));
            builder.append("\")");
            return builder.toString();
        }

        private String getTimeString(JsonObject track) {
            // uts - seconds since Jan 01 1970 UTC
            long uts = 
                Long.valueOf(getString(getObject(track,"date"),"uts"));
            long now = System.currentTimeMillis() / 1000L;
            long seconds = (now - uts);
            int days = (int) TimeUnit.SECONDS.toDays(seconds);
            if (days > 1) {
                return days + " days ago";
            } else if (days == 1) {
                return "yesterday";
            }
            long hTotal = TimeUnit.SECONDS.toHours(seconds);
            long mTotal = TimeUnit.SECONDS.toMinutes(seconds);
            long sTotal = TimeUnit.SECONDS.toSeconds(seconds);
            long h = hTotal - (days*24);
            long m = mTotal - (hTotal*60);
            long s = sTotal - (mTotal*60);
            if (h == 0) {
                if (m == 0) {
                    return s + "s ago";
                }
                return m + "m " + s + "s ago";
            }
            return h + "h " + m + "m " + s + "s ago";
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

    private void loadMap() {
        Path mapFile = Paths.get(MAP_FILE);
        if (Files.notExists(mapFile)) {
            bot.log(Level.WARNING,"No map file was loaded");
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

