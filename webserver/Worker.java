package webserver;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Worker extends Thread {
    Socket socket;
    MimeTypes mimes;
    HttpdConf config;
    TheLogger logger;

    public Worker(Socket socket, MimeTypes mimes, HttpdConf config, TheLogger logger) {
        this.socket = socket;
        this.mimes = mimes;
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            Request request = new Request(inputStream);
            request.parse();
            String requestString = request.getRequestString();
            System.out.println(requestString);
            Resource resource = new Resource(request.getUri(), this.config, this.mimes);
            ResponseFactory.executeRequest(request, resource, outputStream, logger, requestString);
        } catch (IOException ex) {
            
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
