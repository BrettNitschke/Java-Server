package webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import java.text.SimpleDateFormat;
import java.util.*;
import static webserver.ResponseFactory.HTTP_BAD_REQUEST_REASON;

public class Response {
    OutputStream outputStream;
    HashMap<String, String> headers = getBaseHeaders();
    String requestString;
    TheLogger logger;

    public Response(OutputStream outputStream, TheLogger logger, String requestString) {
        this.outputStream = outputStream;
        this.requestString = requestString;
        this.logger = logger;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void sendRequestResponse(int code, String reason) throws IOException {
        Response.sendRequestResponse(code, reason, this.headers, this.outputStream, this.logger, this.requestString);
    }

    public static void sendBadRequestResponse(OutputStream outputStream, TheLogger logger, String requestString) throws IOException {
        sendBasicRequestResponse(HTTP_BAD_REQUEST, HTTP_BAD_REQUEST_REASON, outputStream, logger, requestString);
    }

    public static void sendBasicRequestResponse(int code, String reason, OutputStream outputStream, TheLogger logger, String requestString) throws IOException {
        HashMap<String, String> headers = getBaseHeaders();
        sendRequestResponse(code, reason, headers, outputStream, logger, requestString);
    }

    private static void sendRequestResponse(int code, String reason, HashMap<String, String> headers, OutputStream outputStream, TheLogger logger, String requestString) throws IOException {
        PrintWriter printWriter = new PrintWriter(outputStream);
        String response = responseString(code, reason, headers);
        printWriter.print(response);
        printWriter.flush();
        System.out.println(requestString);
        System.out.println(response);
        logger.write(requestString, response);
    }

    private static HashMap<String, String> getBaseHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Date", getServerTime());
        headers.put("Server", "Gemmell/Nitschke Server");
        return headers;
    }

    private static String getStatusLine(int code, String reasonPhrase) {
        return "HTTP/1.1 " + code + " " + reasonPhrase + "\r\n";
    }

    private static String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    private static String responseString(int code, String reason, HashMap<String, String> headers) {
        String statusLine = getStatusLine(code, reason);
        StringBuilder stringBuilder = new StringBuilder(statusLine);
        for (String key : headers.keySet()) {
            stringBuilder.append(key);
            stringBuilder.append(": ");
            stringBuilder.append(headers.get(key));
            stringBuilder.append("\r\n");
        }
        stringBuilder.append("\r\n");
        return stringBuilder.toString();
    }
}
