package com.nutscape.mc.nunuubot;

import java.io.*;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/* 
 * Program configuration
 */
class Config {

    // User defined fields:

    String nickname = "NunuuBot";
    String realname = "[github.com/McNozes/NunuuBot]";
    String mode = "0";
    String nickPassword = "";
    String exitMessage = "Time to die";
    String serverAddress = "irc.rizon.net";
    int serverPort = 6667;
    int hostPort = 50003;
    boolean useClassReloading = true;
    char specialChar = ':';
    char noModSpecialChar = '.';
    String logStdLevel = "FINE";
    String logFileLevel = "ALL";
    String logFileDir = null;
    int newLogFileAtSizeKB = 5*1024;
    String botsDirname = System.getProperty("user.home") + "/.config/nunuubot";
    String dataDir = botsDirname + "/nunuubot";
    String version = "Im a bot";

    List<String> initModules = Arrays.asList(new String[] {
        "HelloModule",
        "LinkModule",
        "UtilsModule"
    });

    List<String> initChannels = Arrays.asList(new String[] {
        "#McNozes"//,"#bots"
    });

    List<String> admins = Arrays.asList(new String[] {
        ".*!.*@chico.diogo"
    });

    List<String> knownBots = Arrays.asList(new String[] {
        "NunuuBot"
    });

    List<String> ignoreList = Arrays.asList(new String[] {
    });


    List<Pattern> adminsRegex;

    // --------------------------------------

    private void init(String configFilename) {
        /* Define a regex expresion that matches the prefix of a command */

        /* Set the directory to which data files are written.
         * Currently, it will use the filename (without extension) as the
         * subdirectory of either the directory specified in the config file or
         * ~/config/nunuubot.
         * */
        Path path = Paths.get(configFilename);
        String n = path.getFileName().toString();
        String botId = n.substring(0,n.lastIndexOf('.'));
        this.dataDir = botsDirname + "/" + botId;

        if (this.logFileDir == null) {
            this.logFileDir = this.dataDir;
        }

        this.adminsRegex = new ArrayList<Pattern>();
        for (String s : admins) {
            adminsRegex.add(Pattern.compile(s));
        }
    }

    // JSON

    static private ExclusionStrategy exclusion = new ExclusionStrategy()
    {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            List<String> names = Arrays.asList(new String[] {
                "exclusion",
                "adminsRegex"
            });
            return names.contains(f.getName());
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    };

    void writeJSON(String file) throws IOException
    {
        String s = toString();
        Writer out = Files.newBufferedWriter(Paths.get(file));
        for (int i=0; i < s.length(); i++) {
            out.write(s.codePointAt(i));
        }
        if (out != null)
            out.close();
    }

    static Config readJSON(String file) throws IOException
    {
        Gson gson = new GsonBuilder()
            .serializeNulls()
            .addDeserializationExclusionStrategy(exclusion)
            .create();

        Reader in = Files.newBufferedReader(Paths.get(file));
        Config config = gson.fromJson(in,Config.class);
        if (in != null)
            in.close();

        config.init(file);
        return config;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .addSerializationExclusionStrategy(exclusion)
            .create();
        return gson.toJson(this);
    }
}

