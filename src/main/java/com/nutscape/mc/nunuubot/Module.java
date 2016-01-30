package com.nutscape.mc.nunuubot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ExclusionStrategy;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.nutscape.mc.nunuubot.actions.Action;
import com.nutscape.mc.nunuubot.actions.ActionContainer;

/** 
 * Abstract class for bots.
 * TODO: make assynchronous.
 */
public abstract class Module {
    private ActionContainer mPrivMsg,mNotice,mKick; 
    protected Bot bot;

    protected Module(Bot bot) throws ModuleInstantiationException {
        this.bot = bot;
    }

    // ------------------

    // Clients may override this
    public void finish() { }

    // Clients may override this (they can still call the 'superclass' version)
    public void privMsg(IncomingMessage m) {
        if (mPrivMsg != null) { mPrivMsg.acceptAndReturnAtMatch(m); }
    }
    public void notice(IncomingMessage m) {
        if (mNotice != null) { mNotice.acceptAndReturnAtMatch(m); }
    }
    public void kick(IncomingMessage m) {
        if (mKick != null) { mKick.acceptAndReturnAtMatch(m); }
    }

    //private void listenPrivMsg() { this.mPrivmsg = new ActionContainer(); }
    //private void listenNotice() { this.mNotice = new ActionContainer(); }
    //private void listenKick() { this.mKick = new ActionContainer(); }

    protected void addPrivMsgAction(Action action) {
        if (mPrivMsg == null) { mPrivMsg = new ActionContainer(); }
        mPrivMsg.add(action);
    }
    protected void addNoticeAction(Action action) {
        if (mNotice == null) { mNotice = new ActionContainer(); }
        mNotice.add(action);
    }
    protected void addKickAction(Action action) {
        if (mKick == null) { mKick = new ActionContainer(); }
        mKick.add(action);
    }

    // ------------------

    // TODO: move to another class

    /*
     * Module data serialization / deserialization
     */

    protected class ModuleData {
        private JsonObject object;

        private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

        public ModuleData() {
            this.object = new JsonObject();
        }

        Path getSaveFilePath(Class<?> clazz) {
            String className = clazz.getSimpleName();
            String filename = className.replaceAll("Module$","") + ".json";
            Path dir = Paths.get(Module.this.bot.getDataDir());
            return dir.resolve(filename);
        }

        /* Read json file containing Module data. */
        public ModuleData(Class<?> clazz) throws IOException {
            Path path = getSaveFilePath(clazz);
            if (Files.notExists(path))
                throw new NoSuchFileException(path.toString());
            //TODO:
            //bot.log(Level.INFO,"LastfmModule: Loading file " + SAVE_FILE);
            Reader in = Files.newBufferedReader(path);
            JsonObject object = gson.fromJson(in,JsonObject.class);
            in.close();
            this.object = object;
        }

        public boolean has(String key) {
            return object.has(key);
        }

        public <T> T fromJson(String key,Class<T> clazz) {
            JsonElement el = object.get(key);
            return (el == null) ? null : gson.fromJson(el,clazz);
        }

        public void addProperty(String key,String value) {
            object.addProperty(key,value);
        }

        public void add(String key,Object src) {
            object.add(key,gson.toJsonTree(src));
        }

        /* Output a JsonObject representing Module data into a file. */
        public void writeToFile(Class<?> clazz) {
            Path path = getSaveFilePath(clazz);
            try (Writer out = Files.newBufferedWriter(path)) {
                out.write(object.toString());
                out.close();
            } catch (IOException e) {
                // TODO:
                //bot.logThrowable(e);
            }
        }

        public String toString(){
            return gson.toJson(object);
        }
    }

    /*
     * Module instantiation
     */

    public static class ModuleInstantiationException extends Exception { 
        public ModuleInstantiationException(Exception e) { super(e); }
    }

    static Module newModule(
            String shortName,
            String fullName,
            boolean useClassReloading,
            Bot bot) throws Module.ModuleInstantiationException
    {
        try {
            Class<?> cl;
            if (useClassReloading) {
                /* We must use a new ClassLoader each time, otherwise the
                 * JVM just uses the already loaded class definition for
                 * classes and doesn't take into account changes to class
                 * files. */
                ClassLoader parent = ModuleClassLoader.class.getClassLoader();
                ModuleClassLoader loader = new ModuleClassLoader(parent);
                loader.setBot(bot);
                cl = loader.loadClass(fullName);
            } else {
                cl = Class.forName(fullName);
            }
            Constructor<?> constr = cl.getConstructor(Bot.class);
            return (Module) constr.newInstance(bot);

        } catch (InvocationTargetException e) { 
            Throwable cause = e.getCause();
            bot.log(Level.WARNING,"InvocationTargetException: " + cause);
            throw new Module.ModuleInstantiationException(e);
        } catch (ClassNotFoundException  | NoSuchMethodException 
                | InstantiationException | IllegalAccessException e) {
            bot.log(Level.WARNING,"Error initializing " + shortName + ": " +e);
            throw new Module.ModuleInstantiationException(e);
        }
    }

    /* Note: Classes loaded from different classloaders are put in different
     * packages. */
    static class ModuleClassLoader extends ClassLoader {
        protected Bot bot;

        ModuleClassLoader(ClassLoader parent) {
            super(parent);
        }

        void setBot(Bot bot) {
            this.bot = bot;
        }

        @Override
        public Class<?> loadClass(String fullName) 
            throws ClassNotFoundException
        {
            try {
                /*
                 * We have to make sure that this class loader is only called 
                 * for subclasses of Module, even if we didn't call it for
                 * other classes.
                 * This is because java will attempt to load all classes
                 * referenced * by Modules using this loader and, if it does,
                 * they won't be * identified as the same classes as in other
                 * parts of the program.
                 */
                if (!fullName.matches(".*[.][^.]+Module([$].+)?$")) {
                    //System.out.println("doesn't match");
                    //System.out.println(binaryPath);
                    return super.loadClass(fullName);
                }

                /* Get the path to the file containing the class definition */
                String binaryPath =
                    "/" + fullName.replaceAll("[.]","/") + ".class";
                bot.log(Level.FINE,"Reading: " + binaryPath);

                /* Read the class definition. */
                InputStream input = getClass().getResourceAsStream(binaryPath);
                if (input == null) {
                    bot.log(Level.SEVERE,"Module not found.");
                    throw new NullPointerException();
                }
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                for (int data; (data = input.read()) != -1;) {
                    buffer.write(data);
                }
                input.close();
                byte[] classData = buffer.toByteArray();

                return defineClass(fullName,classData,0,classData.length);

            } catch (IOException | NullPointerException e) {
                bot.logThrowable(e);
            }
            return null;
        }
    }
}
