package com.nutscape.mc.nunuubot;

import java.io.*;
import java.util.regex.Pattern;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.StreamHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;
import java.util.logging.SimpleFormatter;
import java.util.logging.Filter;

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

    private static final int NO_FILES = 3;
    private static final boolean APPEND_TO_FILE = true;

    LoggerRunnable(
            String botName,
            BlockingQueue<LogRecord> msgQueue,
            Level globalLevel,
            String fileLogDir,
            Level fileLogLevel,
            int newLogFileAtSizeKB) throws IOException
    {
        this.msgQueue = msgQueue;

        LOGGER.setLevel(globalLevel);
        LOGGER.setUseParentHandlers(false);

        Handler stdlog = new StreamHandler(System.out,new StdOutFormatter()) {
            // publish() doesn't flush by default. We need to override that
            // so that we can debug in realtime.
            @Override
            public synchronized void publish(final LogRecord record) {
                super.publish(record);
                flush();
            }
        };
        // Don't clutter stdout with PONG messages.
        stdlog.setFilter(new Filter() {
            // The ':' is so that CTCP messages don't match.
            private Pattern pongRegex =
                Pattern.compile(".*[^:]PONG .+");

            @Override
            public boolean isLoggable(LogRecord record) {
                return !pongRegex.matcher(record.getMessage()).matches();
            }
        });
        LOGGER.addHandler(stdlog);

        String fileNamePattern = fileLogDir + "/log-" + botName + "%g.txt";
        FileHandler fileLogHandler = new FileHandler(
                fileNamePattern,
                newLogFileAtSizeKB*1024,
                NO_FILES,
                APPEND_TO_FILE);
        fileLogHandler.setEncoding("UTF-8");
        fileLogHandler.setFormatter(new FileFormatter());
        fileLogHandler.setLevel(fileLogLevel);
        LOGGER.addHandler(fileLogHandler);
    }
    // ----------------------------

    private class StdOutFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return record.getMessage() + "\n";
        }
    }

    private class FileFormatter extends Formatter {
        private final DateFormat df =
            new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SS");

        @Override
        public String format(LogRecord record) {
            StringBuilder b = new StringBuilder(1000);
            b.append(df.format(new Date(record.getMillis())));
            b.append(": ").append(record.getLevel());
            b.append(": ").append(formatMessage(record));
            b.append("\n");
            return b.toString();
        }
    }

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
