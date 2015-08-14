package com.nutscape.mc.nunuubot;

import java.nio.file.Path;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/* Note: Classes loaded from different class loaders are put in different
 * packages. */
public class ModuleClassLoader extends ClassLoader {

    public ModuleClassLoader(ClassLoader parent) {
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
