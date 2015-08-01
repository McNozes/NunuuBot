package com.nutscape.mc.nunuubot;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.Socket;
import java.net.InetSocketAddress;

/**
 * Manages the connection to the IRC server.
 *
 * Note: we keep this class out of IRC so that modules don't
 * have access to init().
 */
class Connection {
    private Writer out;

    public Connection() { }

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

        Runnable fetcher = 
            new MessageFetcher(s.getInputStream(),System.out,msgQueue);
        new Thread(fetcher).start();
    }

    public void send(String cmd) throws IOException
    {
        System.out.println("---> " +  cmd);
        out.write(cmd);
        out.write('\n');
        out.flush();
    }

    /** 
     * Reads messages from the server and puts them on a queue.
     * Optionally prints messages to a stream as they are read. */
    static class MessageFetcher implements Runnable {
        private BufferedReader in;
        private PrintStream log;
        private BlockingQueue<String> queue;

        MessageFetcher(
                InputStream in,
                OutputStream out,
                BlockingQueue<String> queue) throws IOException
        {
            this.in = new BufferedReader(new InputStreamReader(in));
            this.log = (out != null) ? new PrintStream(out) : null;
            this.queue = queue;
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
                    if (log != null) {
                        log.println(line);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
