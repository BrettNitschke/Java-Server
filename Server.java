/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import webserver.*;

/**
 *
 * @author brettnitschke
 */
public class Server {
    final String mimeFile = "conf/mime.types";
    final String httpdFile = "conf/bretthttpd.conf";
    String loggerFile;
    MimeTypes mimetypes;
    HttpdConf configuration;
    TheLogger logger;

    public Server() throws FileNotFoundException, IOException {
        this.mimetypes = new MimeTypes(mimeFile);
        this.configuration = new HttpdConf(httpdFile);
    }

    public void start() throws IOException {
        this.configuration.load();
        this.mimetypes.load();
        loggerFile = configuration.getLogFile();
        this.logger = new TheLogger(loggerFile);
        ServerSocket serverSocket = new ServerSocket(getPort());
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                Thread t = new Worker(clientSocket, mimetypes, configuration, logger);
                t.start();
            } catch (IOException e) {
                System.out.println("Could not listen on Port: " + getPort());
            }
        }
    }

    private int getPort() {
        return configuration.getListen();
    }
}
