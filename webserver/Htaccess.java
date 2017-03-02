package webserver;

import java.io.IOException;

public class Htaccess extends ConfigurationReader {
    private Htpassword userFile;
    private String authType = "Basic";
    private String authName = "Default";
    private String require;
    
    public Htaccess(String fileName) throws IOException{
        super(fileName);
        this.load();
    }

    @Override
    public void load() {
        final String authUserFileString = "AuthUserFile";
        final String authTypeString = "AuthType";
        final String authNameString = "AuthName";
        final String requireString = "Require";
        
        String[] split;
        
        try {
            while (hasMoreLines()){
                split = split(nextLine());
                
                switch (split[0]){
                    case authUserFileString:
                        this.userFile = new Htpassword(removeQuotes(split[1]));
                        break;
                    case authTypeString:
                        this.authType = split[1];
                        break;
                    case authNameString:
                        String fullAuthName = "";
                        for (int i =1; i < split.length; i++) {
                            fullAuthName += removeQuotes(split[i]) + " ";
                        }
                        this.authName = fullAuthName;
                        break;
                    case requireString:
                        this.require = split[1];
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e){
            System.out.println("Bad HTaccess");
        } 
    }
    
    public boolean isAuthorized(String authInfo){
        String[] tokens = split(authInfo);
        return userFile.isAuthorized(tokens[1]);
    }
   
    public String getAuthType(){
        return authType;
    }
    
    public String getAuthName(){
        return authName;
    }
    
    public String getRequire(){
        return require;
    }
    
    private String removeQuotes(String string){
        return string.replaceAll("\"", "");
    }
}
