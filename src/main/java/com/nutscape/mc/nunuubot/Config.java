package com.nutscape.mc.nunuubot;

import java.io.*;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.List;
import java.util.Arrays;

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
    String realname = "github.com/McNozes/NunuuBot";
    String mode = "0";
    String nickPassword = "";
    String version = "github.com/McNozes/NunuuBot";
    String serverAddress = "irc.rizon.net";
    int serverPort = 6667;
    int hostPort = 50003;
    boolean useClassReloading = true;
    char specialChar = '\\';
    String logStdLevel = "FINE";
    String logFileLevel = "ALL";
    String logFileDir = ".";
    int newLogFileAtSizeKB = 5*1024;

    List<String> initModules = Arrays.asList(new String[] {
        "HelloModule",
            "LinkModule",
            "UtilsModule"
    });

    List<String> initChannels = Arrays.asList(new String[] {
        "#McNozes"//,"#bots"
    });

    List<String> admins = Arrays.asList(new String[] {
        "McNozes!~McNozes@chico.diogo"
    });

    // Other fields

    String cmdPrefix;

    Config() {
        this.cmdPrefix =  "^(" + nickname + "[-:, ]+|" +
            specialChar + " *)";
    }

    static private ExclusionStrategy exclusion = new ExclusionStrategy()
    {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            List<String> names = Arrays.asList(new String[] {
                "exclusion",
                "cmdPrefix"
            });
            return names.contains(f.getName());
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    };

    void write(String file) throws IOException
    {
        String s = toString();

        Writer out = Files.newBufferedWriter(Paths.get(file));
        for (int i=0; i < s.length(); i++) {
            out.write(s.codePointAt(i));
        }
        if (out != null)
            out.close();
    }

    static Config read(String file) throws IOException
    {
        Gson gson = new GsonBuilder()
            .serializeNulls()
            .addDeserializationExclusionStrategy(exclusion)
        .create();

        Reader in = Files.newBufferedReader(Paths.get(file));
        Config config = gson.fromJson(in,Config.class);
        if (in != null)
            in.close();
        return config;
    }

    public String toString() {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .addSerializationExclusionStrategy(exclusion)
        .create();
        return gson.toJson(this);
    }
}

