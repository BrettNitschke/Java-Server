package webserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

public class ScriptExecuter {

    public static void executeScript(Request request, Resource resource, OutputStream outputStream) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(resource.getAbsolutePath());

        setEnvironment(request, resource.getQueryString(), processBuilder.environment());
        Process process = processBuilder.start();

        OutputStream processInputStream = process.getOutputStream();
        DataOutputStream dataOut = new DataOutputStream(processInputStream);
        if (request.getBody() != null) {
            dataOut.writeBytes(request.getBody());
        }
        dataOut.flush();
        processInputStream.flush();
        processInputStream.close();
        dataOut.close();

        InputStream processOutputStream = process.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(processOutputStream));
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        String lineToPipe;
        if (waitForElementStream(3, processOutputStream)) {
            if (bufferedReader.ready()) bufferedReader.readLine();
            while (bufferedReader.ready()) {
                lineToPipe = bufferedReader.readLine();
                bufferedWriter.write(lineToPipe);
                bufferedWriter.flush();
            }
        }
    }

    private static boolean waitForElementStream(int waitTime, InputStream processOutputStream) throws IOException, InterruptedException {
        long currentTime = System.currentTimeMillis() / 1000;
        long elapsedTime = currentTime + waitTime;
        while (currentTime <= elapsedTime) {
            if (processOutputStream.available() > 0) {
                return true;
            }
            currentTime = System.currentTimeMillis() / 1000;
            Thread.sleep(200);
        }
        return false;
    }

    private static void setEnvironment(Request request, String queryString, Map<String, String> environment) {
        Map<String, String> headers = request.getHeaders();
        for (String key : headers.keySet()) {
            environment.put("HTTP_" + key.toUpperCase(), headers.get(key));
        }
        environment.put("SERVER_PROTOCOL", "HTTP/1.1");
        if (queryString != null) {
            environment.put("QUERY_STRING", queryString);
        }
        environment.put("REQUEST_METHOD", request.getVerb().toUpperCase());
    }
}
