package webserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Request {
    public static String headerContentLength = "content-length";
    public static String ifModifiedSince = "if-modified-since";
    String uri;
    String body;
    String verb;
    String httpVersion;
    Map<String, String> headers = new HashMap<>();
    private boolean valid = false;
    Scanner scanner;
    BufferedReader buffReader;
    String requestString;

    public Request(InputStream inputStream) throws IOException {
        buffReader = new BufferedReader(new InputStreamReader(inputStream));
    }

    public boolean isValid() {
        return valid;
    }

    public boolean parse() {
        String line;

        try {
            line = buffReader.readLine();
            if (line == null) {
                System.out.println("Request *empty*");
                return false;
            }

            requestString = line + "\r\n";

            String[] splitted = line.split("\\s+");
            if (splitted.length < 3) {
                return false;
            }

            verb = splitted[0];
            uri = splitted[1];
            httpVersion = splitted[2];

            line = buffReader.readLine();

            while (!line.isEmpty()) {
                requestString += line + "\r\n";
                String[] splitted2 = line.split(": ");
                headers.put(splitted2[0].toLowerCase(), splitted2[1]);

                line = buffReader.readLine();
            }

            if (headers.containsKey(headerContentLength)) {
                String contentLength = headers.get(headerContentLength);
                int contentLengthInt = Integer.parseInt(contentLength);
                char[] charBuff = new char[contentLengthInt];
                buffReader.read(charBuff, 0, contentLengthInt);
                body = new String(charBuff);
                requestString += "\r\n";
                requestString += body;
            }
        } catch (IOException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        valid = true;
        return true;
    }

    public String getBody() {
        return body;
    }

    public String getVerb() {
        return verb;
    }

    public String getUri() {
        return uri;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getHeaderForKey(String key) {
        return headers.get(key);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean containsAuthorizationHeader() {
        return (headers.containsKey("authorization"));
    }

    public String getAuthorizationHeaderValue() {
        return headers.get("authorization");
    }

    public String getRequestString() {
        return requestString;
    }
}
