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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.LogRecord;
import java.util.logging.Level;

class ModuleInstantiationException extends Exception { 
    ModuleInstantiationException(Exception e) { super(e); }
}

class StopExecutingException extends Exception { }

public class NunuuBot implements BotInterface {

    // SETTINGS
    // TODO: log file names here.

    private static final String CONFIG_FILE = "config.json";

    private static final String VERSION_NUMBER = "0.3";

    // Must not end in '/':
    private static final String MODULES_DIR = "modules";

    // ----------------------------------

    // Prefix for the full path to module classes:
    private static final String MODULES_PREFIX =
        NunuuBot.class.getCanonicalName().replaceAll("[.][^.]+$","") +
        "." + MODULES_DIR.replaceAll("/",".");


    // TODO: remove Connection from here
    // TODO: use java's Observer and Observable
    private Config config;
    private final Connection connection = new Connection(this);
    private final IRC irc;
    private final Map<String,Module> modules = new HashMap();
    private final BlockingQueue<LogRecord> logQueue
        = new LinkedBlockingQueue<>();
    private Thread loggerThread;
    private Thread connectionThread;

    /* Constructor halts if a single module fails to initialize.  */
    private NunuuBot(Config config) throws Exception
    {
        log(Level.FINE,"************** STARTING **************");

        this.config = config;
        log(Level.FINER,config.toString());

        this.irc = new IRC(connection);

        // Start modules specified in the config
        for (String m : config.initModules) {
            loadModule(m);
        }
    }

    // ----------

    private void processNotice(IncomingMessage m) {
        for (Map.Entry<String,Module> e : modules.entrySet()) {
            Module mod = e.getValue();
            if (mod instanceof NoticeReceiver) {
                ((NoticeReceiver)mod).notice(m);
            }
        }
    }

    private void processPrivMessage(IncomingMessage m)
        throws StopExecutingException
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
                return;
            } else {
                // Redirect messages to admins
                for (String ad : config.admins) {
                    irc.sendPrivMessage(ad,m.getNick() + ": "+ m.getContent());
                }
            }
        }

        // Send message to all modules.
        for (Map.Entry<String,Module> entry : modules.entrySet()) {
            entry.getValue().privMsg(m);
        }
    }

    private void processLine(String line,long timestamp) 
        throws IOException, StopExecutingException
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
                if (!config.nickPassword.equals("")) {
                    irc.nickservIdentify(config.nickPassword);
                }
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
                log(Level.FINE,"Modules: loading statically");
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
                throw new StopExecutingException();
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

    public void logThrowable(Throwable e) {
        log(Level.SEVERE,e.getClass().getSimpleName() + ": " + e.getMessage());
        for (StackTraceElement s : e.getStackTrace()) {
            log(Level.FINE,s.toString());
        }
    }

    // Main

    private void run()
    {
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>();

        try {
            // Start logging in its own thread
            this.loggerThread = new Thread(new LoggerRunnable(
                        this.getClass().getSimpleName(),
                        logQueue,
                        Level.parse(config.logStdLevel),
                        config.logFileDir,
                        Level.parse(config.logFileLevel),
                        config.newLogFileAtSizeKB));
            this.loggerThread.start();

            // Connect to the server
            this.connectionThread = this.connection.start(
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
                        break; // success; break the infinite cycle
                    } catch (InterruptedException e) { }
                }
                try {
                    processLine(line,millis);
                } catch (StopExecutingException e) {
                    break;
                } catch (Exception e) {
                    logThrowable(e);
                }
            }

            // Finish program
            new Finisher().run();

        } catch (IOException e) {
            logThrowable(e);
        }
    }

    private class Finisher implements Runnable {
        @Override public void run() {
            finishAllModules();
            irc.quit("Goodbye");
            //connectionThread.interrupt();
            //loggerThread.interrupt();
        }

        void finishAllModules() {
            for (Map.Entry<String,Module> e : modules.entrySet()) {
                e.getValue().finish();
            }
        }
    }

    public static void main(String[] args)
    {
        try {

            // Read config file
            Config newConfig = Config.read(CONFIG_FILE);

            // Program instance
            NunuuBot nunuubot = new NunuuBot(newConfig);

            // Control+C hook
            Runtime.getRuntime().addShutdownHook(
                    new Thread(nunuubot.new Finisher()));

            // Run the program (connect to server, start threads, etc)
            nunuubot.run();

        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
