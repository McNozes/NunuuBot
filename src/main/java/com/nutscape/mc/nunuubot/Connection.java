package com.nutscape.mc.nunuubot;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.logging.Level;

/**
 * Manages the connection to the IRC server.
 *
 * Note: we keep this class out of IRC so that modules don't
 * have access to init().
 */
class Connection {
    private Writer out;
    private Bot bot;

    Connection(Bot bot) {
        this.bot = bot;
    }

    // ------------

    Thread start(
            String serverAddress,
            int serverPort,
            int hostPort,
            BlockingQueue<String> msgQueue) throws IOException
    {
        Socket s = new Socket();
        s.setReuseAddress(true);
        s.bind(new InetSocketAddress(hostPort));
        s.connect(new InetSocketAddress(serverAddress,serverPort));
        this.out = new OutputStreamWriter(s.getOutputStream());

        Runnable fetcher = 
            new MessageFetcher(s.getInputStream(),msgQueue,bot,s);
        Thread thread = new Thread(fetcher);
        thread.start();
        return thread;
    }

    void send(String cmd) throws IOException
    {
        bot.log(Level.INFO,"> " +  cmd);
        out.write(cmd);
        out.write('\n');
        out.flush();
    }

    /** 
     * Reads messages from the server and puts them on a queue.
     * Optionally prints messages to a stream as they are read. */
    static class MessageFetcher implements Runnable {
        private BufferedReader in;
        private BlockingQueue<String> queue;
        private Bot bot;
        private boolean stop = false;
        private Socket socket;

        MessageFetcher(InputStream in,BlockingQueue<String> queue,Bot bot,
                Socket socket)
            throws IOException
        {
            this.in = new BufferedReader(new InputStreamReader(in));
            this.queue = queue;
            this.bot = bot;
            this.socket = socket;
        }

        public void run() {
            try {
                while (true) {
                    String line = in.readLine();
                    if (line == null) { // TODO: figure out reason for nulls
                        bot.log(Level.SEVERE,"Null line in MessageFetcher");
                        break;
                    }
                        queue.put(line);
                    bot.log(Level.FINE,line);
                }
            } catch (IOException e) {
                bot.logThrowable(e);
            } catch (InterruptedException e) { }

            try {
                socket.close();
            } catch (IOException e) {
                bot.logThrowable(e);
            }
            bot.log(Level.FINER,"Finishing Connection thread");
        }
    }
}
