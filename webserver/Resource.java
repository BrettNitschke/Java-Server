package webserver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Resource {
    private String absolutePath;
    private boolean isScript;
    private boolean isValid = false;
    private String mimeType;
    private String queryString;
    private HttpdConf config;

    

    public String getMimeType() {
        return mimeType;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public Resource(String uriString, HttpdConf config, MimeTypes mimes) {
        this.config = config;
        if (null == uriString || uriString.isEmpty()) {
            return;
        }
        isScript = false;
        URI uri;

        try {
            uri = new URI(uriString);
            queryString = uri.getQuery();
            String uriPath = uri.getPath();

            if (uriPath.contains(".")) {
                String extension = uriPath.substring(uriPath.lastIndexOf(".") + 1);
                this.mimeType = mimes.lookUp(extension);
            }

            String uriPathFirstDirectory = getFirstUriDirectoryFromPath(uri.getPath());
            if (config.aliasForKeyExists(uriPathFirstDirectory)) {
                absolutePath = uriPath.replace(uriPathFirstDirectory, config.getAliasForKey(uriPathFirstDirectory));
            } else if (config.scriptAliasForKeyExists(uriPathFirstDirectory)) {
                absolutePath = uriPath.replace(uriPathFirstDirectory, config.getScriptAliasForKey(uriPathFirstDirectory));
                isScript = true;
            } else {
                absolutePath = concatinateWithoutDoubleSlashes(config.getDocumentRoot(), uriPath);
            }

            if (!isFile(absolutePath)) {
                absolutePath += config.getDirectoryIndex();
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(Resource.class.getName()).log(Level.SEVERE, null, ex);
        }

        isValid = true;
    }

    private String getFirstUriDirectoryFromPath(String uriPath) {
        String[] split = uriPath.split("/");
        return "/" + split[1] + "/";
    }

    private String concatinateWithoutDoubleSlashes(String root, String uriPath) {
        if (root.endsWith("/") && uriPath.startsWith("/")) {
            return root + uriPath.substring(1);
        }
        return root + uriPath;
    }

    public boolean isFile(String path) {
        return !path.substring(path.length() - 1).equals("/");
    }

    public boolean isScript() {
        return this.isScript;
    }

    public String getAbsolutePath() {
        return this.absolutePath;
    }

    public String getQueryString() {
        return this.queryString;
    }

    public String getHtAccessFileName() {
        return config.getAccessFileName();
    }

    
}
