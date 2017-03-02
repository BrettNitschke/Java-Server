package webserver;
import java.io.*;

public class Authenticator {
    private Htaccess htaccess;
    Request request;
    Resource resource;
    String htaccessFileName;
    String path;
    String pathWithHtaccessFile;
    String htaccessFileLocation;
    
    public Authenticator(Request request, Resource resource) {
        this.request = request;
        this.resource = resource;
        this.htaccessFileName = resource.getHtAccessFileName();
        this.path = resource.getAbsolutePath();
    }
    
    public boolean doesHtaccessFileExist() throws IOException{
        String[] splitPath = path.split("/");
        return searchEachDirectoryForHtaccess(splitPath);
    }
    
    private boolean searchEachDirectoryForHtaccess(String[] splitPath) throws IOException{
        String subPath = "/";
        
        for (int i =1; i< splitPath.length; i++){
            subPath += splitPath[i] + "/";
             String subPathFileName = subPath + htaccessFileName;
             File file = new File(subPathFileName);
             if (file.isFile()){
                 htaccessFileLocation = subPathFileName;
                 htaccess = new Htaccess(htaccessFileLocation);
                 return true;
             }
        }
        return false;
    }
    
    public void loadHtaccessFile() throws IOException{
        htaccess = new Htaccess(htaccessFileLocation);
    }
    
    public boolean doesRequestHaveAuthHeader(){
         return request.containsAuthorizationHeader();
    }
    
    public boolean checkForAuthentication() throws IOException{
        String authorizationHeaderValue = request.getAuthorizationHeaderValue();
        return htaccess.isAuthorized(authorizationHeaderValue);
    }
    
    public String getAuthName(){
        return htaccess.getAuthName();
    }
    
    public String getAuthType(){
        return htaccess.getAuthType();
    }  
}
