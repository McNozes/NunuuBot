package com.nutscape.mc.nunuubot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;
import java.nio.file.Path;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** 
 * Abstract class for bots.
 * TODO: make assynchronous.
 */
public abstract class Module {
    public static class ModuleInstantiationException extends Exception { 
        public ModuleInstantiationException(Exception e) { super(e); }
    }

    protected BotInterface bot;
    protected IRC irc;    // Output interface.

    public Module(IRC irc,BotInterface bot)
        throws ModuleInstantiationException {
        this.bot = bot;
        this.irc = irc;
    }

    // ------------------

    public void finish() {
        // Do nothing
    }

    public abstract void privMsg(IncomingMessage msg);

    protected boolean match(Pattern p,String s) {
        return p.matcher(s).matches();
    }

    /*
     * Factory method.
     */
    static Module newModule(
            String shortName,
            String fullName,
            boolean useClassReloading,
            IRC irc,
            BotInterface bot) throws ModuleInstantiationException {
        try {
            Class<?> cl;
            if (useClassReloading) {
                /* We must use a new ClassLoader each time, otherwise the
                 * JVM just uses the already loaded class definition for
                 * classes and doesn't take into account changes to class
                 * files. */
                ClassLoader parent = ModuleClassLoader.class.getClassLoader();
                ModuleClassLoader loader = new ModuleClassLoader(parent);
                cl = loader.loadClass(fullName);
            } else {
                cl = Class.forName(fullName);
            }
            Constructor<?> constr = cl.getConstructor(
                        IRC.class,BotInterface.class);
            return (Module) constr.newInstance(irc,bot);

        } catch (InvocationTargetException e) { 
            Throwable cause = e.getCause();
            System.err.println("InvocationTargetException: " + cause);
            throw new ModuleInstantiationException(e);
        } catch (ClassNotFoundException  | NoSuchMethodException 
                | InstantiationException | IllegalAccessException e) {
            System.err.println("Error initializing " + shortName + ": " + e);
            throw new ModuleInstantiationException(e);
        }
    }

    /* Note: Classes loaded from different classloaders are put in different
     * packages. */
    static class ModuleClassLoader extends ClassLoader {

        ModuleClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> loadClass(String fullName) throws ClassNotFoundException {
            try {
                /*
                 * We have to make sure that this class loader is only called for
                 * Module subclasses, even if we didn't call it for other classes.
                 * This is because java will attempt to load classes referenced
                 * by Modules using this loader and, if it does, they won't be
                 * identified as the same classes as in other parts of the
                 * program.
                 */
                if (!fullName.matches(".*[.][^.]+Module([$].+)?$")) {
                    //System.out.println("doesn't match");
                    //System.out.println(binaryPath);
                    return super.loadClass(fullName);
                }

                /* Get the path to the file containing the class definition */
                String binaryPath =
                    "/" + fullName.replaceAll("[.]","/") + ".class";
                System.out.println("Reading: " + binaryPath);

                /* Read the class definition. */
                InputStream input = getClass().getResourceAsStream(binaryPath);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                for (int data; (data = input.read()) != -1;) {
                    buffer.write(data);
                }
                input.close();
                byte[] classData = buffer.toByteArray();

                return defineClass(fullName,classData,0,classData.length);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

