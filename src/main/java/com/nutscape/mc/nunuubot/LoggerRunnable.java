package com.nutscape.mc.nunuubot;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.StreamHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;
import java.util.logging.SimpleFormatter;

//import static java.nio.file.StandardOpenOption.*;
//import static java.nio.file.StandardCopyOption.*;

/**
 * Logger class.
 * Uses the java.util.logging package.
 * The logging levels in descending order are:
 * - SEVERE
 * - WARNING
 * - INFO
 * - CONFIG
 * - FINE
 * - FINER
 * - FINEST
 *
 * This class runs in its own thread, and writes log information into
 * standard output as well as to a file. When said file is big enough, a
 * new one is created. Old log files are deleted.
 */
class LoggerRunnable implements Runnable {
    private BlockingQueue<LogRecord> msgQueue;
    private final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    LoggerRunnable(
            BlockingQueue<LogRecord> msgQueue,
            Level globalLevel,
            String fileLogName,
            Level fileLogLevel,
            int newLogFileAtSizeKB) throws IOException
    {
        this.msgQueue = msgQueue;

        LOGGER.setLevel(globalLevel);
        LOGGER.setUseParentHandlers(false);

        Handler stdlog = new StreamHandler(System.out,new StdOutFormatter()) {
            @Override
            public synchronized void publish(final LogRecord record) {
                super.publish(record);
                flush();
            }
        };
        LOGGER.addHandler(stdlog);

        int noFiles = 2;
        boolean append = true;
        String fileNamePattern = fileLogName;
        FileHandler fileLogHandler = new FileHandler(
                fileNamePattern,newLogFileAtSizeKB*1024,noFiles,append);
        //fileLogHandler.setFormatter();
        fileLogHandler.setEncoding("UTF-8");
        fileLogHandler.setLevel(fileLogLevel);
        //LOGGER.addHandler(fileLogHandler);
    }
    // ----------------------------

    private class StdOutFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return record.getMessage() + "\n";
        }
    }

    /*
       private OutputStream getLogFileStream(String name,newFileSizeKB)
       throws IOException
   {
   Path path = Paths.get(name);
    // Rename old log file if size too big
    if (Files.exists(path) &&
    Files.size(path) > (newFileSizeKB*1024)) {
    Path pathOld = Paths.get(name + ".old");
    Files.move(path,pathOld,REPLACE_EXISTING);
    }
    return Files.newOutputStream(path,CREATE,APPEND);
   }
   */

    public void run()
    {
        while (true) {
            try {
                LogRecord rec = msgQueue.take();
                LOGGER.log(rec);
            } catch (InterruptedException e) { }
        }
    }
}
