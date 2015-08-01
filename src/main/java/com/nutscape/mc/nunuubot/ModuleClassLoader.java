package com.nutscape.mc.nunuubot;

import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/* Note: Classes loaded from different class loaders are put in different
 * packages. */
public class ModuleClassLoader extends ClassLoader {

    public ModuleClassLoader(ClassLoader parent) {
        super(parent);
    }

    private static final String PATH =
        ModuleClassLoader.class.getCanonicalName().replaceAll(".ModuleClassLoader$","");

    static final String classpath = System
        .getProperties()
        .getProperty("java.class.path");


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

            String binaryPath =
                "/" + fullName.replaceAll("[.]","/") + ".class";
            System.out.println("Reading: " + binaryPath);

            /* Read the class definition. Note that this won't work if
             * we have a jar. */
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


    public Class<?> loadClassOld(String name) throws ClassNotFoundException {
        try {
            /* Get the path to the file containing the class definition */
            String binaryPath = classpath
                + "/" + name.replaceAll("[.]","/") + ".class";
            Path path = FileSystems.getDefault().getPath(binaryPath);

            /*
             * We have to make sure that this class loader is only called for
             * Module subclasses, even if we didn't call it for other classes.
             * This is because java will attempt to load classes referenced
             * by Modules using this loader and, if it does, they won't be
             * identified as the same classes as in other parts of the
             * program.
             */
            if (!binaryPath.matches(".*/[^/]+Module([$].*)?\\.class$")) {
                //System.out.println("doesn't match");
                //System.out.println(binaryPath);
                return super.loadClass(name);
            }
            if (Files.notExists(path)) {
                //System.out.println("notExists");
                return super.loadClass(name);
            }

            System.out.println("Reading class file for " + name);

            /* Read the class definition. Note that this won't work if
             * we have a jar. */
            InputStream input = Files.newInputStream(path);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            for (int data; (data = input.read()) != -1;) {
                buffer.write(data);
            }
            input.close();
            byte[] classData = buffer.toByteArray();

            return defineClass(name,classData,0,classData.length);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPath() {
        System.out.println(PATH);
        return PATH;
    }

}
