package webserver;

import java.io.*;
import java.util.*;

public class MimeTypes extends ConfigurationReader {
    private HashMap<String, String> types;

    public MimeTypes(String FileName) throws FileNotFoundException {
        super(FileName);
        types = new HashMap<>();
        
    }
    
    public String lookUp(String extension){
        
        String mediaType = types.get(extension);
        return  mediaType;
        
    }

    @Override
    public void load() {
        String line;
        
        while (hasMoreLines()) {
            line = nextLine();
            if (isValid(line)) {
                String[] split = split(line);
                if (split.length > 1) {
                    for (int iterator = 1; iterator < split.length; iterator++) {
                        types.put(split[iterator], split[0]);
                    }
                }
            }
        }
    }

    
    
    private boolean isValid(String toBeChecked){
       if (toBeChecked.contains("#")){
           return false;
       }
       return true;
    }
}
