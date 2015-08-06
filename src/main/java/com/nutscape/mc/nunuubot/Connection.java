package com.nutscape.mc.nunuubot;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
    private BotInterface bot;

    Connection(BotInterface bot) {
        this.bot = bot;
    }

    // ------------

    public void init(
            String serverAddress,
            int serverPort,
            int hostPort,
            BlockingQueue<String> msgQueue) throws IOException
    {
        Socket s = new Socket();
        s.bind(new InetSocketAddress(hostPort));
        s.connect(new InetSocketAddress(serverAddress,serverPort));
        this.out = new OutputStreamWriter(s.getOutputStream());

        Runnable fetcher = new MessageFetcher(s.getInputStream(),msgQueue,bot);
        new Thread(fetcher).start();
    }

    public void send(String cmd) throws IOException
    {
        bot.log(Level.INFO,"---> " +  cmd);
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
        private BotInterface bot;

        MessageFetcher(InputStream in,BlockingQueue<String> queue,
                BotInterface bot)
            throws IOException
        {
            this.in = new BufferedReader(new InputStreamReader(in));
            this.queue = queue;
            this.bot = bot;
        }

        public void run() {
            try {
                while (true) {
                    String line = in.readLine();
                    try {
                        queue.put(line);
                    } catch (InterruptedException e) {
                        System.err.println("Interrupted: " + e);
                    }
                    bot.log(Level.FINE,line);
                }
            } catch (IOException e) {
                bot.log(Level.SEVERE,e.toString());
            }
        }
    }
}
