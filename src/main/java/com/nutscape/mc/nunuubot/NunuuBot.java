package com.nutscape.mc.nunuubot;

import java.io.*;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.LogRecord;
import java.util.logging.Level;

class ModuleInstantiationException extends Exception { 
    ModuleInstantiationException(Exception e) { super(e); }
}

public class NunuuBot implements BotInterface {

    // SETTINGS

    private static final String VERSION_NUMBER = "0.1";

    // Must not end in '/':
    private static final String MODULES_DIR = "modules";

    // ----------------------------------

    // Prefix for the full path to module classes:
    private static final String MODULES_PREFIX =
        NunuuBot.class.getCanonicalName().replaceAll("[.][^.]+$","") +
        "." + MODULES_DIR.replaceAll("/",".");

    // TODO: move into config file
    private static class Config {
        private String nickname = "NunuuBot";
        private String realname = "github.com/McNozes/NunuuBot";
        private String mode = "0";
        private String nickPassword = "";
        private String version = "github.com/McNozes/NunuuBot";
        private String serverAddress = "irc.rizon.net";
        private int serverPort = 6667;
        private int hostPort = 50003;
        private boolean useClassReloading = true;
        private char specialChar = '\\';
        private String logStdLevel = "FINE";
        private String logFileLevel = "ALL";
        private String logFileName = "log";
        private int newLogFileAtSizeKB = 5*1024;

        private List<String> initModules = Arrays.asList(new String[] {
            "HelloModule",
            "LinkModule",
            "UtilsModule"
        });

        private List<String> initChannels = Arrays.asList(new String[] {
            "#McNozes"//,"#bots"
        });

        private List<String> admins = Arrays.asList(new String[] {
            "McNozes!~McNozes@chico.diogo"
        });

        private String cmdPrefix;

        Config() {
            this.cmdPrefix =  "^(" + nickname + "[-:, ]+|" +
                specialChar + " *)";
        }
    }

    // TODO: make final
    private Config config;
    private IRC irc;
    private Connection connection; // TODO: remove from here
    private Map<String,Module> modules;
    private BlockingQueue<LogRecord> logQueue;
    // TODO: use java's Observer and Observable

    /* 
     * This constructor halts if a single module fails to initialize.
     */
    private NunuuBot(Config config) throws Exception
    {
        this.config = config;

        this.logQueue = new LinkedBlockingQueue<>();
        this.connection = new Connection(this);
        this.irc = new IRC(connection);

        // Start modules specified in the config
        this.modules = new HashMap();
        for (String m : config.initModules) {
            loadModule(m);
        }
    }

    // ----------

    private void processNotice(IncomingMessage m) {
        // TODO: catch exceptions
        for (Map.Entry<String,Module> e : modules.entrySet()) {
            Module mod = e.getValue();
            if (mod instanceof NoticeReceiver) {
                ((NoticeReceiver)mod).notice(m);
            }
        }
    }

    private void processPrivMessage(IncomingMessage m)
    {
        //if (msg.equals("\001VERSION\001")) {
        //    irc.sendNotice(prefix,"\001VERSION " + config.version
        //            + " " + VERSION_NUMBER + "\001");
        //    return;
        //}

        if (m.getDestination().equals(config.nickname)) {
            if (config.admins.contains(m.getPrefix())) {
                // Commands - admin only
                adminCommand(m);
            } else {
                // Redirect messages to admins
                for (String ad : config.admins) {
                    irc.sendPrivMessage(ad,m.getNick() + ": "+ m.getContent());
                }
            }
            return;
        }

        // Send message to all modules.
        // TODO: catch exceptions
        for (Map.Entry<String,Module> entry : modules.entrySet()) {
            entry.getValue().privMsg(m);
        }
    }

    private void processLine(String line,long timestamp) throws IOException
    {
        IncomingMessage m = new IncomingMessage(line,timestamp);
        switch (m.getCommand())
        {
            case "PING":  // TODO: check if it's my prefix?
                irc.pong(m.getArguments());
                break;
            case "NOTICE":
                processNotice(m);
                break;
            case "PRIVMSG":
                processPrivMessage(m);
                break;
            case "PONG":
                System.out.println("Received pong...");
                break;
            case "001":
                irc.nickservIdentify(config.nickPassword); // TODO conditional
                for (String channel : config.initChannels) {
                    irc.join(channel);
                }
                break;
            default:
                break;
        }
    }

    private void loadModule(String shortName)
        throws ModuleInstantiationException
    {
        // TODO: do a module name check 

        // Loading a loaded module is equivalent to reloading.
        if (modules.containsKey(shortName)) {
            unloadModule(shortName);
        }
        
        String fullName = MODULES_PREFIX + "." + shortName;

        try {
            Class<?> cl;
            if (config.useClassReloading) {
                /* We must use a new ClassLoader each time, otherwise the
                 * JVM just uses the already loaded class definition for
                 * classes and doesn't take into account changes to class
                 * files. */
                ClassLoader parent = ModuleClassLoader.class.getClassLoader();
                ModuleClassLoader loader = new ModuleClassLoader(parent);
                cl = loader.loadClass(fullName);
            } else {
                System.out.println("loading statically");
                cl = Class.forName(fullName);
            }
            Constructor<?> constr = cl.getConstructor(
                        IRC.class,BotInterface.class);
            Module h = (Module) constr.newInstance(irc,this);
            this.modules.put(shortName,h);

        } catch (InvocationTargetException e) { 
            Throwable cause = e.getCause();
            System.err.println("InvocationTargetException: " + cause);
            throw new ModuleInstantiationException(e);
        } catch (ClassNotFoundException  | NoSuchMethodException 
                | InstantiationException | IllegalAccessException e) {
            System.err.println("Error initializing " + shortName + ": " + e);
            throw new ModuleInstantiationException(e);
        }
        System.out.println("loaded " + shortName);
    }

    private boolean unloadModule(String moduleName) {
        Module mod = this.modules.remove(moduleName);
        if (mod == null) {
            System.err.println("Module not found: " + moduleName);
            return false;
        }
        mod.finish();
        System.out.println("Unloaded " + moduleName);
        return true;
    }

    private void adminCommand(IncomingMessage m) {
        String[] cmd = m.getContent().split(" +",2);
        switch (cmd[0]) {
            case "load": case "l":
                if (!config.useClassReloading)
                    return;
                try {
                    loadModule(cmd[1]);
                } catch (ModuleInstantiationException e) {
                    irc.sendPrivMessage(m.getNick(),
                            "error: " + e.getCause().getMessage());
                }
                break;
            case "unload": case "u":
                if (!config.useClassReloading)
                    return;
                if (!unloadModule(cmd[1])) {
                    irc.sendPrivMessage(m.getNick(), "error unloading module");
                }
                break;
            case "msg": case "m":
                String[] parts = cmd[1].split(" +",2);
                irc.sendPrivMessage(parts[0],parts[1]);
                break;
            case "join": case "j":
                irc.join(cmd[1]);
                break;
            case "part": case "p":
                irc.part(cmd[1]);
                break;
            case "version":
                irc.sendPrivMessage(cmd[1],"\001VERSION\001");
                break;
            case "quit":
                // TODO
                break;
            default:
                break;
        }
    }

    // BotInterface

    public String getNickname() {
        return config.nickname;
    }

    public String getSpecialChar() {
        return config.nickname;
    }

    public String getCmdPrefix() {
        return config.cmdPrefix;
    }

    public void log(Level level,String msg) {
        try {
            logQueue.put(new LogRecord(level,msg));
        } catch (InterruptedException e) {
            System.err.println("Error writing to log");
        }
    }

    // Main

    private void run()
    {
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>();

        try {
            new Thread(new LoggerRunnable(
                        logQueue,
                        Level.parse(config.logStdLevel),
                        config.logFileName,
                        Level.parse(config.logFileLevel),
                        config.newLogFileAtSizeKB)).start();

            this.connection.init(
                    config.serverAddress,
                    config.serverPort,
                    config.hostPort,
                    msgQueue);

            irc.sendUser(config.nickname,config.mode,config.realname);
            irc.sendNick(config.nickname);

            while (true) {
                String line;
                long millis;
                while (true) { // try again if interrupted
                    try {
                        line = msgQueue.take();
                        millis = System.currentTimeMillis();
                        break;
                    } catch (InterruptedException e) { }
                }
                processLine(line,millis);
            }

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        new NunuuBot(new Config()).run();
    }
}


