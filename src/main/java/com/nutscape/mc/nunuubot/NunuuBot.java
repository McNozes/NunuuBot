package com.nutscape.mc.nunuubot;

import java.io.*;
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

class ModuleInstantiationException extends Exception { 
    ModuleInstantiationException(Exception e) { super(e); }
}

public class NunuuBot {

    // SETTINGS

    public static final String VERSION_NUMBER = "0.1";

    // Must not end in '/':
    private static final String MODULES_DIR = "modules";

    // ----------------------------------

    // Prefix for the full path to module classes:
    private static final String MODULES_PREFIX = NunuuBot.class
        .getCanonicalName().replaceAll("[.][^.]+$","")
        + "." + MODULES_DIR.replaceAll("/",".");

    static class Config {    // TODO: move into config file
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
    }

    class InitModuleConfig implements ModuleConfig {
        public String getNickname() {
            return config.nickname;
        }
    }

    private Config config;
    private ModuleConfig moduleConfig;
    private IRC irc;
    private Connection connection; // TODO: remove from here
    private Map<String,Module> modules;

    public NunuuBot(Config config)
    {
        this.connection = new Connection();
        this.irc = new IRC(this.connection);

        this.config = config;
        this.moduleConfig = new InitModuleConfig();

        // Start modules specified in the config
        this.modules = new HashMap();
        for (String m : config.initModules) {
            try { 
                loadModule(m);
            } catch (Exception e) { 
                System.err.println(e);
            }
        }
    }

    // ----------

    private void processNotice(String prefix,String dest,String msg) {
        for (Map.Entry<String,Module> e : modules.entrySet()) {
            Module m = e.getValue();
            if (m instanceof Notices) {
                ((Notices)m).getNotice(prefix,dest,msg);
            }
        }
    }

    private void processMessage(String prefix,String dest,String msg)
    {
        if (msg.equals("\001VERSION\001")) {
            irc.sendNotice(prefix,"\001VERSION " + config.version
                    + " " + VERSION_NUMBER + "\001");
            return;
        }

        if (dest.equals(config.nickname)) {
            if (config.admins.contains(prefix)) {
                // Commands - admin only
                adminCommand(prefix,msg);
            } else {
                // Redirect messages to admins
                for (String ad : config.admins) {
                    irc.sendMessage(ad,dest + ": " + msg);
                }
            }
            return;
        }

        // Send message to all modules.
        for (Map.Entry<String,Module> e : modules.entrySet()) {
            e.getValue().privMsg(prefix,dest,msg);
        }
    }

    private void processLine(String line) throws IOException
    {
        String prefix;
        String command;
        String rest;
        if (line.charAt(0) == ':') {   // there's a prefix
            String[] cmds = line.split(" +",3);
            prefix = cmds[0].substring(1);
            command = cmds[1];
            rest = cmds[2];
        } else {
            String[] cmds = line.split(" +",2);
            prefix = null;
            command = cmds[0];
            rest = cmds[1];
        }
        //System.out.println("> " + command);

        switch (command)
        {
            case "PING":  // TODO: check if it's my prefix?
                irc.pong(rest.substring(1));
                break;

            case "NOTICE":
            case "PRIVMSG":
                String[] parts = rest.split(" ",2);
                String dest = parts[0];
                String msg = parts[1].substring(1);
                if (command.equals("PRIVMSG")) {
                    processMessage(prefix,dest,msg);
                } else {
                    processNotice(prefix,dest,msg);
                }

                break;

            case "PONG":
                for (Map.Entry<String,Module> e : modules.entrySet()) {
                    Module m = e.getValue();
                    if (m instanceof Pinger) {
                        ((Pinger)m).getPonged(prefix);
                    }
                }
                break;

            case "001":
                irc.nickservIdentify(config.nickPassword);
                for (String channel : config.initChannels) {
                    irc.join(channel);
                }
                break;

            default:
                break;
        }
    }

    private void run()
    {
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>();

        try {
            this.connection.init(
                    config.serverAddress,
                    config.serverPort,
                    config.hostPort,
                    msgQueue);

            irc.sendUser(config.nickname,config.mode,config.realname);
            irc.sendNick(config.nickname);

            while (true) {
                String line;
                while (true) { // try again if interrupted
                    try {
                        line = msgQueue.take();
                        break;
                    } catch (InterruptedException e) { }
                }
                processLine(line);
            }

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
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
                // We must use a new ClassLoader each time, otherwise the
                // JVM just uses the already loaded class definition for
                // classes and doesn't take into account changes to class
                // files.
                ClassLoader parent = ModuleClassLoader.class.getClassLoader();
                ModuleClassLoader loader = new ModuleClassLoader(parent);
                cl = loader.loadClass(fullName);
            } else {
                System.out.println("loading statically");
                cl = Class.forName(fullName);
            }
            Constructor<?> constr = cl.getConstructor(
                        IRC.class,ModuleConfig.class);
            Module h = (Module) constr.newInstance(irc,moduleConfig);
            this.modules.put(shortName,h);

        } catch (InvocationTargetException e) { 
            Throwable cause = e.getCause();
            System.err.println("Cause: " + cause);
            throw new ModuleInstantiationException(e);
        } catch (ClassNotFoundException  | NoSuchMethodException 
                | InstantiationException | IllegalAccessException e) {
            System.err.println("error initializing " + shortName + ": " + e);
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

    private void adminCommand(String prefix,String msg) {
        String[] cmd = msg.split(" +",2);
        switch (cmd[0]) {
            case "load":
                if (!config.useClassReloading)
                    return;
                try {
                    loadModule(cmd[1]);
                } catch (ModuleInstantiationException e) {
                    irc.sendMessage(prefix,
                            "error: " + e.getCause().getMessage());
                }
                break;
            case "unload":
                if (!config.useClassReloading)
                    return;
                if (!unloadModule(cmd[1])) {
                    irc.sendMessage(prefix, "error unloading module");
                }
                break;
            case "msg":
                String[] parts = cmd[1].split(" +",2);
                irc.sendMessage(parts[0],parts[1]);
                break;
            case "join":
                irc.join(cmd[1]);
                break;
            case "part":
                irc.part(cmd[1]);
            case "version":
                irc.sendMessage(cmd[1],"\001VERSION\001");
            default:
                break;
        }
    }

    public static void main(String[] args) throws Exception {
        new NunuuBot(new Config()).run();
    }

}
