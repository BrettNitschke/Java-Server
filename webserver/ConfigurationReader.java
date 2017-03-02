package webserver;
import java.io.*;
import java.util.*;

public  abstract class ConfigurationReader  {
    File file;
    Scanner scan;
    
    public  ConfigurationReader(String fileName) throws FileNotFoundException {
        file = new File(fileName);
        scan = new Scanner(file);
    }
    
    protected boolean hasMoreLines(){
        return scan.hasNextLine();
    }
    
    protected String nextLine(){
        if (hasMoreLines()){
            String line = scan.nextLine();  
            return line;
        }   
        return null;
    }
    
    protected String[] split(String toBeSplit){
        String[] split = toBeSplit.split("\\s");
        return split;
    }
    
    public abstract void load();
}
