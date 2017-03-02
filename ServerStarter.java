/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import webserver.*;

/**
 *
 * @author brettnitschke
 */
public class ServerStarter {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        Server server = new Server();
        server.start();
    }
}
