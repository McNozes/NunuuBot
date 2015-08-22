package com.nutscape.mc.nunuubot.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.nutscape.mc.nunuubot.BotInterface;
import com.nutscape.mc.nunuubot.IRC;
import com.nutscape.mc.nunuubot.IncomingMessage;
import com.nutscape.mc.nunuubot.Module;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.ActionContainer;
import com.nutscape.mc.nunuubot.actions.CommandFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.net.ConnectException ;

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
    private final String API_URL = "http://ws.audioscrobbler.com/2.0/";
    private final String SAVE_FILE = "data/Lastfm.json";

    private String API_KEY = null;
    private String API_SECRET = null;
    private Map<String,String> userMap;
    private int newUsers = 0;
    private static final int SAVE_USERS_INTERVAL = 10;

    public LastfmModule(IRC irc,BotInterface bot) 
        throws ModuleInstantiationException
    {
        super(irc,bot);
        try {
            /* We use an Exception to signal that the module does not have
             * everything it needs to work. */
            loadSaveFile();
        } catch (Exception e) {
            throw new ModuleInstantiationException(e);
        }

        CommandFactory fac = new CommandFactory(bot.getCmdPrefix());
        fac.setIRC(irc);
        String pf = "((lf)|(lfm)|(fm))";
        String mapString = bot.getSpecialChar() + "lfmset";
        addCommand((fac.newMappedCommand(pf+"count",
                    userMap,mapString,playCountAction)));
        addCommand(fac.newMappedCommand(pf+"?np",
                    userMap,mapString,new NowPlayingAction(false)));
        addCommand(fac.newMappedCommand(pf+"?npalbum",
                    userMap,mapString,new NowPlayingAction(true)));
        addCommand(fac.newMapPutCommand(pf+"set",userMap,saveMap));
        addCommand(fac.newDoubleMappedCommand(pf+"?compare",
                    userMap,mapString,compareAction));
    }
    // -----------

    /* Show a user's play count. */
    private Action playCountAction = new Action() {
        @Override
        public boolean accept(IncomingMessage m,String... args) {
            try {
                String nick = args[0];
                String target = args[1];
                URL url = formBaseRequestURL("user.getInfo","&user=" + target);
                JsonObject resp = makeJsonRequest(url);
                if (resp.has("error")) {
                    handleResponseError(resp,m);
                    return true;
                }
                JsonObject userObject = resp.get("user").getAsJsonObject();
                String user = userObject.get("name").getAsString();
                String count = userObject.get("playcount").getAsString();
                //String registered = userObject.get("registered").getAsJsonObject()
                //    .get("#text").getAsString()
                //    .replaceAll(" \\d\\d:\\d\\d$","."); // API screw up
                String timeString = userObject.get("registered")
                    .getAsJsonObject()
                    .get("unixtime").getAsString();
                String formattedDate = unixtimeToDateString(timeString);

                irc.sendPrivMessage(m.getDestination(),
                        nick + ": " + count + " tracks played since " 
                        + formattedDate);
            } catch (IOException e) {
                bot.logThrowable(e);
            }
            return true;
        }
    };

    private String unixtimeToDateString(String timeString) {
        long uts = Long.valueOf(timeString) * 1000L;
        Date date = new Date(uts);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(date);
    }

    /* Show a comparison between two users. */
    private Action compareAction = new Action() {
        public static final String ARTISTS_LIMIT = "5";

        @Override
        public boolean accept(IncomingMessage m,String... args) {
            try {
                String nick1 = args[0];
                String nick2 = args[1];
                String user1 = args[2];
                String user2 = args[3];
                String type1 = "user";
                String type2 = "user";
                URL url = formBaseRequestURL("tasteometer.compare",
                        "&type1=" + type1 +
                        "&type2=" + type2 +
                        "&value1=" + user1 +
                        "&value2=" + user2 +
                        "&limit=" + ARTISTS_LIMIT);
                JsonObject resp = makeJsonRequest(url);
                // Check for errors
                if (resp.has("error")) {
                    handleResponseError(resp,m);
                    return true;
                }
                StringBuilder msg = new StringBuilder();
                JsonObject obj = 
                    getObject(getObject(resp,"comparison"),"result");
                Double score = 100.0*Double.valueOf(getString(obj,"score"));
                String scoreString = String.format("%.2f",score);

                msg.append(nick1);
                msg.append(" is ");
                msg.append(IRC.Colors.bold(scoreString.toString()));
                msg.append("% compatible with ");
                msg.append(nick2);
                msg.append(". Artists in common: ");
                JsonArray artistsArray = 
                    getObject(obj,"artists").get("artist").getAsJsonArray();
                for (JsonElement artistElement : artistsArray) {
                    JsonObject artistObject = artistElement.getAsJsonObject();
                    String name = getString(artistObject,"name");
                    msg.append(IRC.Colors.bold(name));
                    msg.append(", ");
                }
                msg.setLength(msg.length()-2);

                irc.sendPrivMessage(m.getDestination(),msg.toString());
            } catch (IOException e) {
                bot.logThrowable(e);
            }
            return true;
        }
    };

    /* Show what the user is playing or has played */
    private class NowPlayingAction extends Action {
        private boolean album;

        NowPlayingAction(boolean album) {
            this.album = album;
        }

        @Override 
        public boolean accept(IncomingMessage m,String... args) {
            try {
                String nick = args[0];
                String target = args[1];
                URL url = formBaseRequestURL("user.getRecentTracks",
                        "&user=" + target + "&limit=1");

                JsonObject resp = makeJsonRequest(url);
                // Check for errors
                if (resp.has("error")) {
                    handleResponseError(resp,m);
                    return true;
                }
                StringBuilder msg = new StringBuilder();
                JsonElement el = getObject(resp,"recenttracks").get("track");
                if (!el.isJsonArray()) {
                    JsonObject track = el.getAsJsonObject();
                    msg.append(nick);
                    msg.append(" played ");
                    msg.append(getTrackString(track));
                    msg.append(" ");
                    long uts = 
                        Long.valueOf(getString(getObject(track,"date"),"uts"));
                    msg.append(getTimeString(uts));
                } else {
                    // LastFM returns an array with two tracks when the track
                    // is currently playing.
                    JsonArray array = el.getAsJsonArray();
                    // It's the second element
                    JsonObject track = array.get(0).getAsJsonObject();
                    msg.append(nick);
                    msg.append(" is now playing ");
                    msg.append(getTrackString(track));
                }

                //bot.log(Level.INFO,track.toString());
                irc.sendPrivMessage(m.getDestination(),msg.toString());

            } catch (IOException e) {
                bot.logThrowable(e);
            }
            return true;
        }

        private String getTrackString(JsonObject track)
        {
            StringBuilder builder = new StringBuilder();
            builder.append(IRC.Colors.bold(getString(track,"name")));
            builder.append(" by ");
            builder.append(IRC.Colors.bold(
                        getString(getObject(track,"artist"),"#text")));
            if (album) {
                builder.append(" (");
                builder.append(IRC.Colors.bold(
                            getString(getObject(track,"album"),"#text")));
                builder.append(")");
            }
            return builder.toString();
        }

        private String getTimeString(long uts) {
            // uts - seconds since Jan 01 1970 UTC
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
            return h + "h " + m + "m ago";
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
        bot.log(Level.WARNING,"last.fm: "+ error + ": " + message);
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
        @Override public boolean accept(IncomingMessage m,String...args) {
            newUsers++;
            if (SAVE_USERS_INTERVAL == newUsers) {
                newUsers = 0;
                writeSaveFile();
            }
            return true;
        }
    };

    private void loadSaveFile() throws Exception {
        try {
            ModuleData save = new ModuleData(LastfmModule.class);
            this.API_KEY = save.fromJson("API_KEY",String.class);
            if (API_KEY == null) {
                throw new Exception("Savefile is missing API key.");
            }
            this.API_SECRET = save.fromJson("API_SECRET",String.class);
            this.userMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            if (save.has("userMap")) {
                this.userMap = save.fromJson("userMap",userMap.getClass());
            } else {
                bot.log(Level.WARNING,"No user map was found in file");
            }
        } catch (NoSuchFileException e) {
            throw e;
        } catch (IOException e) {
            bot.logThrowable(e);
        }
    }

    private void writeSaveFile() {
        ModuleData save = new ModuleData();
        save.addProperty("API_KEY",API_KEY);
        save.addProperty("API_SECRET",API_SECRET);
        save.add("userMap",userMap);
        save.writeToFile(LastfmModule.class);
    }

    @Override
    public void finish() {
        writeSaveFile();
    }
}

