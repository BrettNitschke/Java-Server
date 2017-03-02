package webserver;

import java.io.*;
import static java.net.HttpURLConnection.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

public class ResponseFactory {
    static String HTTP_OK_REASON = "OK";
    static String HTTP_CREATED_REASON = "Created";
    static String HTTP_INTERNAL_ERROR_REASON = "Internal Server Error";
    static String HTTP_NOT_FOUND_REASON = "Not Found";
    static String HTTP_FORBIDDEN_REASON = "Forbidden";
    static String HTTP_NO_CONTENT_REASON = "No Content";
    static String HTTP_BAD_REQUEST_REASON = "Bad request";
    static String HTTP_NOT_MODIFIED_REASON = "Not Modified";
    static String HTTP_UNAUTHORIZED_REASON = "Unauthorized";

    public static void executeRequest(Request request, Resource resource, OutputStream outputStream, TheLogger logger, String requestString) throws IOException {
        if (!request.isValid() || !resource.isValid()) {
            Response.sendBadRequestResponse(outputStream, logger, requestString);
            return;
        }

        Authenticator authenticator = new Authenticator(request, resource);
        try {
            if (authenticator.doesHtaccessFileExist()) {
                if (!authenticator.doesRequestHaveAuthHeader()) {
                    String authType = authenticator.getAuthType();
                    String authName = authenticator.getAuthName();
                    String authKey = "WWW-Authenticate";
                    String authValue = authType + " realm=" + "\"" + authName + "\"";

                    Response response = new Response(outputStream, logger, requestString);
                    response.addHeader(authKey, authValue);
                    response.sendRequestResponse(HTTP_UNAUTHORIZED, HTTP_UNAUTHORIZED_REASON);
                    return;
                }

                if (!authenticator.checkForAuthentication()) {
                    Response.sendBasicRequestResponse(HTTP_FORBIDDEN, HTTP_FORBIDDEN_REASON, outputStream, logger, requestString);
                    return;
                }
            }
        } catch (IOException exception) {
            Response.sendBasicRequestResponse(HTTP_INTERNAL_ERROR, "htaccess error", outputStream, logger, requestString);
            return;
        }

        if (resource.isScript()) {
            try {
                Response.sendBasicRequestResponse(HTTP_OK, HTTP_OK_REASON, outputStream, logger, requestString);
                ScriptExecuter.executeScript(request, resource, outputStream);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(ResponseFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {

            switch (request.getVerb()) {
                case "PUT":
                    put(request, resource, outputStream, logger, requestString);
                    break;
                case "DELETE":
                    delete(resource, outputStream, logger, requestString);
                    break;
                case "GET":
                    get(request, resource, outputStream, true, logger, requestString);
                    break;
                case "POST":
                    post(request, resource, outputStream, logger, requestString);
                    break;
                case "HEAD":
                    head(request, resource, outputStream, logger, requestString);
                    break;
                default:
                    Response.sendBadRequestResponse(outputStream, logger, requestString);
            }
        }

        try {
            outputStream.flush();
        } catch (IOException ex) {
            Logger.getLogger(ResponseFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void delete(Resource resource, OutputStream outputStream, TheLogger logger, String requestString) throws IOException {
        try {
            Path fullPath = Paths.get(resource.getAbsolutePath());
            Files.delete(fullPath);
            Response.sendBasicRequestResponse(HTTP_NO_CONTENT, HTTP_NO_CONTENT_REASON, outputStream, logger, requestString);
        } catch (NoSuchFileException exception) {
            Response.sendBasicRequestResponse(HTTP_NOT_FOUND, HTTP_NOT_FOUND_REASON, outputStream, logger, requestString);
        } catch (DirectoryNotEmptyException exception) {
            Response.sendBasicRequestResponse(HTTP_FORBIDDEN, "Deleting Directory Forbidden", outputStream, logger, requestString);
        } catch (IOException exception) {
            Response.sendBasicRequestResponse(HTTP_INTERNAL_ERROR, HTTP_INTERNAL_ERROR_REASON, outputStream, logger, requestString);
        }
    }

    private static void get(Request request, Resource resource, OutputStream outputStream, boolean sendBody, TheLogger logger, String requestString) throws IOException {
        try {
            if (isCached(request, resource)) {
                Response.sendBasicRequestResponse(HTTP_NOT_MODIFIED, HTTP_NOT_MODIFIED_REASON, outputStream, logger, requestString);
                return;
            }
        } catch (ParseException ex) {
            Response.sendBasicRequestResponse(HTTP_INTERNAL_ERROR, HTTP_INTERNAL_ERROR_REASON, outputStream, logger, requestString);
            return;
        }

        File file = new File(resource.getAbsolutePath());
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(resource.getAbsolutePath());
                Response response = new Response(outputStream, logger, requestString);
                System.out.println("Total file size to read (in bytes) : " + fileInputStream.available());
                response.addHeader("Content-Length", Integer.toString(fileInputStream.available()));
                response.addHeader("Content-Type", resource.getMimeType());
                response.sendRequestResponse(HTTP_OK, HTTP_OK_REASON);

                if (sendBody) {
                    byte[] buffer = new byte[1024];
                    int numberOfBytes = 0;
                    while ((numberOfBytes = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, numberOfBytes);
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ResponseFactory.class.getName()).log(Level.SEVERE, null, ex);
                Response.sendBasicRequestResponse(HTTP_NOT_FOUND, HTTP_NOT_FOUND_REASON, outputStream, logger, requestString);
            } catch (IOException ex) {
                Logger.getLogger(ResponseFactory.class.getName()).log(Level.SEVERE, null, ex);
                Response.sendBasicRequestResponse(HTTP_INTERNAL_ERROR, HTTP_INTERNAL_ERROR_REASON, outputStream, logger, requestString);
            }
        } else {
            Response.sendBasicRequestResponse(HTTP_NOT_FOUND, HTTP_NOT_FOUND_REASON, outputStream, logger, requestString);
        }
    }

    private static boolean isCached(Request request, Resource resource) throws ParseException, IOException {
        String modifiedSinceDateString = request.getHeaderForKey(Request.ifModifiedSince);
        if (null != modifiedSinceDateString) {
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
            Date modifiedSinceDate = format.parse(modifiedSinceDateString);

            Path path = Paths.get(resource.getAbsolutePath());
            BasicFileAttributes attributes;
            attributes = Files.readAttributes(path, BasicFileAttributes.class);
            SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy'-'mm'-'dd'T'HH':'mm':'ss'Z'");
            Date fileModifiedSinceDate = fileFormat.parse(attributes.lastModifiedTime().toString());

            if (fileModifiedSinceDate.before(modifiedSinceDate)) {
                return true;
            }
        }
        return false;
    }

    private static void put(Request request, Resource resource, OutputStream outputStream, TheLogger logger, String requestString) throws IOException {
        if (request.getBody() == null) {
            Response.sendBasicRequestResponse(HTTP_BAD_REQUEST, "Empty body in PUT", outputStream, logger, requestString);
            return;
        }
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(resource.getAbsolutePath()));
            dataOutputStream.writeBytes(request.getBody());
            dataOutputStream.close();
            Response.sendBasicRequestResponse(HTTP_CREATED, HTTP_CREATED_REASON, outputStream, logger, requestString);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ResponseFactory.class.getName()).log(Level.SEVERE, null, ex);
            Response.sendBasicRequestResponse(HTTP_INTERNAL_ERROR, "Creating File Failed", outputStream, logger, requestString);
        } catch (IOException ex) {
            Logger.getLogger(ResponseFactory.class.getName()).log(Level.SEVERE, null, ex);
            Response.sendBasicRequestResponse(HTTP_INTERNAL_ERROR, "Writing bytes to file failed", outputStream, logger, requestString);
        }
    }

    private static void post(Request request, Resource resource, OutputStream outputStream, TheLogger logger, String requestString) throws IOException {
        get(request, resource, outputStream, true, logger, requestString);
    }

    private static void head(Request request, Resource resource, OutputStream outputStream, TheLogger logger, String requestString) throws IOException {
        get(request, resource, outputStream, false, logger, requestString);
    }
}
