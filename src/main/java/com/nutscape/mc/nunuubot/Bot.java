package com.nutscape.mc.nunuubot;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;
import java.util.Collection;

/**
 * Main program class.
 * Exposes (non-static) services such as logging and IRC publicly.
 *
 * - Bot asks for config file (or uses the default one 
 *     ~/.config/nunuubot/nunuubot.json).
 * - BotID := name of config file minus the extension
 * - BotDir will be in the config file
 *   (otherwise, BotDir := ~/.config/nunuubot)
 * - All output data will go into BotDir/BotID/ 
 *   (this assumes that there is no file named BotID in BotDir)
 * - Module configuration is 
 *
 * @author(McNozes)
 */
public class Bot {
    class StopExecutingException extends Exception { }

    /*
     * Settings
     */
    private static final String VERSION_NUMBER = "0.4.1";
    private static final String SOURCE_STRING = "github.com/McNozes/NunuuBot";
    private String VERSION_STRING = SOURCE_STRING + " " + VERSION_NUMBER;

    private static String configFilename = "~/.config/nunuubot/nunuutest.json";
    static {
        // Replace home
        configFilename = configFilename.replaceAll("^~/",
                System.getProperty("user.home") + "/");
    }

    // Prefix for the full path to module classes:
    // TODO: use Path
    private static final String MODULES_DIRNAME = "modules";
    private static final String MODULES_PREFIX =
        Bot.class.getCanonicalName().replaceAll("[.][^.]+$","") +
        "." + MODULES_DIRNAME.replace("/+$","").replaceAll("/",".");

    // ----------------------------------

    /*
     * Bot Interface
     */

    public String getNickname()  { return config.nickname; }
    public char getSpecialChar() { return config.specialChar; }
    public String getCmdPrefix() { return config.cmdPrefix; }
    public String getDataDir()   { return config.dataDir; }
    public Collection<String> getKnownBots() { return config.knownBots; }
    public IRC getIRC()          { return irc; }

    public String getVersion()   {
        return config.version + " " + VERSION_NUMBER; }

    public void log(Level level,String msg) {
        try {
            logQueue.put(new LogRecord(level,msg));
        } catch (InterruptedException e) {
            System.err.println("Error writing to log");
        }
    }

    public void logThrowable(Throwable e) {
        log(Level.SEVERE,e.getClass().getSimpleName() + ": " 
                + e.getMessage());
        for (StackTraceElement s : e.getStackTrace()) {
            log(Level.FINE,s.toString());
        }
    }

    // ----------------------------------

    // TODO: remove Connection from here
    // TODO: use java's Observer and Observable
    private Config config;
    private final Connection connection;
    private final IRC irc;
    private final Map<String,Module> modules = new HashMap();
    private final BlockingQueue<LogRecord> logQueue;
    private Thread loggerThread;
    private Thread connectionThread;
    private final Admin admin = new Admin();

    private final long timeoutMillis = 10*60*1000; // minutes*seconds*millis
    private long timeOfLastPing;
    private boolean reboot = false;

    /* This constructor will halt if a single module fails to initialize. */
    private Bot(Config config) throws Exception
    {
        this.logQueue = new LinkedBlockingQueue<>();
        log(Level.FINE,"************** START OF LOG **************");

        this.connection = new Connection(this);
        this.irc = new IRC(connection,this);

        this.config = config;
        log(Level.FINER,config.toString());
        Path configsDir = Paths.get(config.dataDir);
        if (!Files.isDirectory(configsDir)) {
            Files.createDirectory(configsDir);
        }

        // Start modules specified in the config
        for (String m : config.initModules) {
            loadModule(m);
        }
    }

    // ----------

    private boolean doReboot() {
        return this.reboot;
    }

    private void processInput(String line,long timestamp) 
        throws IOException, StopExecutingException
    {
        IncomingMessage m = new IncomingMessage(line,timestamp);

        switch (m.getCommand())
        {
            case "NOTICE":
                for (Map.Entry<String,Module> e : modules.entrySet()) {
                    Module mod = e.getValue();
                    mod.notice(m);
                }
                break;

            case "PRIVMSG":
                if (m.getDestination().equals(config.nickname)) {
                    admin.privMsg(m);
                }
                // Send message to all modules.
                for (Map.Entry<String,Module> entry : modules.entrySet()) {
                    entry.getValue().privMsg(m);
                }
                break;

            case "KICK":
                for (Map.Entry<String,Module> entry : modules.entrySet()) {
                    entry.getValue().kick(m);
                }
                break;

            case "PING":
                timeOfLastPing = timestamp;
                irc.pong(m.getArguments());
                break;

            case "001":
                // Identify
                if (!config.nickPassword.equals("")) {
                    irc.nickservIdentify(config.nickPassword);
                }
                // Join all channels
                for (String channel : config.initChannels) {
                    irc.join(channel);
                }
                break;
            default:
                break;
        }
    }

    private void loadModule(String shortName)
        throws Module.ModuleInstantiationException {
        // TODO: do a module name check 

        // Loading a loaded module is equivalent to reloading.
        if (modules.containsKey(shortName)) {
            unloadModule(shortName);
        }
        String fullName = MODULES_PREFIX + "." + shortName + "Module";
        Module m = Module.newModule(shortName+"Module",
                fullName,config.useClassReloading,this);
        modules.put(shortName,m);
        log(Level.INFO,"Modules: loaded " + shortName);
    }

    private boolean unloadModule(String moduleName) {
        Module mod = this.modules.remove(moduleName);
        if (mod == null) {
            System.err.println("Module not found: " + moduleName);
            return false;
        }
        mod.finish();
        log(Level.INFO,"Unloaded " + moduleName);
        return true;
    }

    private class Admin {

        private void privMsg(IncomingMessage m) throws StopExecutingException {
            if (hasAdminPrivileges(m)) {
                // Process command - admin only
                adminCommand(m);
                return;
            } else {
                // Redirect message to admins
                for (String ad : config.admins) {
                    irc.sendPrivMessage(ad,m.getNick() + ": "+ m.getContent());
                }
            }
        }

        private boolean hasAdminPrivileges(IncomingMessage m) {
            String prefix = m.getPrefix();

            for (Pattern p : config.adminsRegex) {
                if (p.matcher(prefix).matches())
                    return true;
            }
            return false;
        }

        private void adminCommand(IncomingMessage m)
            throws StopExecutingException
        {
            String[] cmd = m.getContent().split(" +",2);
            switch (cmd[0]) {

                case "load": case "l":
                    if (!config.useClassReloading)
                        return;
                    try {
                        loadModule(cmd[1]);
                    } catch (Module.ModuleInstantiationException e) {
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
                    throw new StopExecutingException();

                default:
                    break;
            }
        }
    }


    // Main

    private void run()
    {
        try {
            // Start logging in its own thread
            this.loggerThread = new Thread(new LoggerRunnable(
                        getNickname(),
                        logQueue,
                        Level.parse(config.logStdLevel),
                        config.logFileDir,
                        Level.parse(config.logFileLevel),
                        config.newLogFileAtSizeKB));
            this.loggerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>();
        try {
            // Connect to the server
            this.connectionThread = this.connection.start(
                    config.serverAddress,
                    config.serverPort,
                    config.hostPort,
                    msgQueue);

            // Control+C hook
            Runtime.getRuntime().addShutdownHook(new Thread(new Finisher()));
        } catch (Exception e) {
            logThrowable(e);
        }

        irc.sendUser(config.nickname,config.mode,config.realname);
        irc.sendNick(config.nickname);

        timeOfLastPing = System.currentTimeMillis();
        reboot = false;

        while (true) {
            String line;
            long millis;
            while (true) { // try again if interrupted
                try {
                    line = msgQueue.take();
                    millis = System.currentTimeMillis();
                    break; // success; break the infinite cycle
                } catch (InterruptedException e) { }
            }

            long dtPing = millis - timeOfLastPing;
            if (dtPing > timeoutMillis) {
                reboot = true;
                log(Level.INFO,"Rebooting; delta millis = " + dtPing);
                break;
            }

            try {
                processInput(line,millis);
            } catch (StopExecutingException e) {
                break;
            } catch (Exception e) {
                logThrowable(e);
            }
        }

        // Finish program
        new Finisher().run();
    }

    private class Finisher implements Runnable {
        @Override public void run() {
            finishAllModules();
            irc.quit(config.exitMessage);
            connectionThread.interrupt();
            loggerThread.interrupt();
        }

        void finishAllModules() {
            for (Map.Entry<String,Module> e : modules.entrySet()) {
                e.getValue().finish();
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        for (int i=0; i < args.length; i++) {
            if (args[i].equals("-c") && ++i < args.length) {
                configFilename = args[i];
            }
        }

        // Read config file
        Config newConfig = Config.readJSON(configFilename);

        // Program instance
        Bot bot = new Bot(newConfig);

        // Run the program (connect to server, start threads, etc)
        do {
            bot.run();
        } while (bot.doReboot());
    }
}
