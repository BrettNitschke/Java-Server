package webserver;
import java.io.*;

public  class TheLogger {
    File log;
    Writer logWriter;
    String loggerFileName;
    
    public  TheLogger(String loggerFileName) throws IOException{
        this.loggerFileName = loggerFileName;
        log = new File(loggerFileName);
        
        if(!log.exists()){
            log.createNewFile();
        }  
    }
    
    public synchronized  void write(String request, String response) throws IOException{
        if (request != null && response != null) {
        logWriter = new BufferedWriter(new FileWriter(loggerFileName, true));
        logWriter.write("\r\n");
        logWriter.write(request);
        logWriter.flush();
        logWriter.write("\r\n");
        logWriter.write("\r\n");
        logWriter.write(response);
        logWriter.flush();
        logWriter.write("\r\n");
        logWriter.close();
        }
    }
}
