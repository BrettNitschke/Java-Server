package webserver;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class HttpdConf extends ConfigurationReader {
    private final HashMap<String, String> aliases;
    private final HashMap<String, String> scriptAliases;
    private String serverRoot;
    private String documentRoot;
    private int listen = 8080;
    private String logFile = "log.txt";
    private String accessFileName = ".htaccess";
    private String directoryIndex = "index.html";
    
    public HttpdConf(String fileName) throws FileNotFoundException {
        super(fileName);
        aliases  = new HashMap<>();
        scriptAliases  = new HashMap<>();
    }

    @Override
    public void load() {
        final String serverRootString = "ServerRoot";
        final String documentRootString = "DocumentRoot";
        final String listenString = "Listen";
        final String logFileString = "LogFile";
        final String scriptAliasString = "ScriptAlias";
        final String aliasString = "Alias";
        final String accessFileNameString = "AccessFileName";
        final String directoryIndexString = "DirectoryIndex";
        
        String[] split;
        try {
            while(hasMoreLines()){
                split = split(nextLine());
                switch (split[0]) {
                    case serverRootString:
                        this.serverRoot = removeQuotes(split[1]);
                        break;
                    case documentRootString:
                        this.documentRoot = removeQuotes(split[1]);
                        break;
                    case listenString:
                        this.listen = Integer.parseInt(split[1]);
                        break;
                    case logFileString:
                        this.logFile = removeQuotes(split[1]);
                        break;
                    case scriptAliasString:
                        this.scriptAliases.put(removeQuotes(split[1]), removeQuotes(split[2]));
                        break;
                    case aliasString:
                        this.aliases.put(removeQuotes(split[1]), removeQuotes(split[2]));
                        break;
                    case accessFileNameString:
                        this.accessFileName = removeQuotes(split[1]);
                        break;
                    case directoryIndexString:
                        this.directoryIndex = removeQuotes(split[1]);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e){
            System.out.println("Bad HttpdConf");
        }
    }
    
    private String removeQuotes(String string){
        return string.replaceAll("\"", "");
    }
    
    public String getServerRoot(){
         return serverRoot;
    }
    
    public String getDocumentRoot(){
         return documentRoot;
    }
    
    public int getListen(){
         return listen;
    }
    
    public String getLogFile(){
         return logFile;
    }

    public boolean aliasForKeyExists(String key) {
         return aliases.containsKey(key);
    }
    
    public String getAliasForKey(String key) {
         return aliases.get(key);
    }
    
    public boolean scriptAliasForKeyExists(String key) {
         return scriptAliases.containsKey(key);
    }
    
    public String getScriptAliasForKey(String key) {
         return scriptAliases.get(key);
    }
    
    public String getAccessFileName(){
         return this.accessFileName;
    }
    
    public String getDirectoryIndex(){
         return this.directoryIndex;
    }
}
