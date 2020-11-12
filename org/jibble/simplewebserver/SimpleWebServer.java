/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of Mini Wegb Server / SimpleWebServer.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/
*/

package org.jibble.simplewebserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright Paul Mutton
 * http://www.jibble.org/
 * This version is available at https://github.com/rafaelsteil/simple-webserver
 */
public class SimpleWebServer extends Thread {
    private File rootDir;
    private ServerSocket serverSocket;
    private boolean _debug = false;
    private boolean _report = false;
    private boolean _running = true;

    public static final String VERSION = "SimpleWebServer  http://www.jibble.org/";
    public static final Map<String, String> MIME_TYPES = new HashMap<String, String>();
    
    static {
        String image = "image/";
        MIME_TYPES.put(".gif", image + "gif");
        MIME_TYPES.put(".jpg", image + "jpeg");
        MIME_TYPES.put(".jpeg", image + "jpeg");
        MIME_TYPES.put(".png", image + "png");
        MIME_TYPES.put(".bmp", image + "bmp");
        MIME_TYPES.put(".ico", image + "x-icon");
        MIME_TYPES.put(".cur", image + "x-icon");
        MIME_TYPES.put(".svg", image + "svg+xml");
        MIME_TYPES.put(".tiff", image + "tiff");
        
        String text = "text/";
        MIME_TYPES.put(".html", text + "html");
        MIME_TYPES.put(".htm", text + "html");
        MIME_TYPES.put(".txt", text + "plain");
        MIME_TYPES.put(".css", text + "css");

        MIME_TYPES.put(".xml", "application/xml");
        MIME_TYPES.put(".json", "application/json");
        MIME_TYPES.put(".js", "application/javascript");
    }
    
    /**
     * Starts the webserver
     * @param rootDir The root directory where the server should run
     * @param port the port to listen at
     * @throws IOException
     */
    public SimpleWebServer(File rootDir, int port, boolean dbg, boolean rep) throws IOException {

        this._debug = dbg;
	this._report = rep;
        this.rootDir = rootDir.getCanonicalFile();
        
        if (!this.rootDir.isDirectory()) {
            throw new IOException("Not a directory.");
        }
        
        serverSocket = new ServerSocket(port);
        start();
    }
    
    /**
     * Changes the root directory where the server runs.
     * This method can be called while the server is running, and it will take effect right 
     * in the next request.  
     * @param rootDir the new directory.
     */
    public void setRootDir(File rootDir) {
    	this.rootDir = rootDir;
    }
    
    /**
     * Stops the webserver
     */
    public void stopServer() {
    	try {
    		serverSocket.close();
    		interrupt();
    	}
    	catch (Exception e) {}
    }

    public void run() {
        while (_running) {
            try {
                Socket socket = serverSocket.accept();
                RequestThread requestThread = new RequestThread(socket, rootDir);
                requestThread.setReporting(_debug, _report);
                requestThread.start();
            }
            catch (Exception e) {
                System.exit(1);
            }
        }
    }

    public static void usage() {
       System.out.println("Usage: SimpleWebServer.jar --port=<portnr> --dir=<directory to serve> [--debug=<true|false>] [--report=<true|false>]");
       System.exit(0);
    }
    
    public static void main(String[] args) {
        int port = -1;
        String dir = "";
        boolean dbg = false;
        boolean rep = false;

        int i, n;
        for(i = 0, n = args.length; i < n; i++) {
            String keyval = args[i];
            String[] parts = keyval.split("=");
            if (parts.length == 2) {
               String key = parts[0];
               String value = parts[1];
               if (key.equals("--port")) { port = Integer.parseInt(value); }
               else if (key.equals("--dir")) { dir = value; }
               else if (key.equals("--help")) { SimpleWebServer.usage(); }
               else if (key.equals("--debug")) { dbg = Boolean.parseBoolean(value); }
               else if (key.equals("--report")) { rep = Boolean.parseBoolean(value); }
               else { System.out.println("Unknown option: " + key); System.exit(1); }
            }
        }

        if (port <= 0 || dir =="") { SimpleWebServer.usage(); }


        try {
            new SimpleWebServer(new File(dir), port, dbg, rep);
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
}
