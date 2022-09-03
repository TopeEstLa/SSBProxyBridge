package com.bgsoftware.ssbproxybridge.core.http;

import com.bgsoftware.ssbproxybridge.core.Singleton;
import com.bgsoftware.ssbproxybridge.core.connector.ConnectionFailureException;
import com.bgsoftware.ssbproxybridge.core.connector.ConnectorAbstract;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class HttpConnector extends ConnectorAbstract<HttpConnectionArguments> {

    private static final Gson gson = new Gson();

    private static final ExecutorService HTTP_CONNECTOR_EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setNameFormat("SSBProxyBridge Http Connection").build());

    private static final Singleton<HttpConnector> SINGLETON = new Singleton<HttpConnector>() {
        @Override
        protected HttpConnector create() {
            return new HttpConnector();
        }
    };

    private static final Logger logger = Logger.getLogger("SSBProxyBridge");

    private String url;
    private String secret;

    public static HttpConnector getConnector() {
        return SINGLETON.get();
    }

    private HttpConnector() {

    }

    @Override
    public void connect(HttpConnectionArguments args) throws ConnectionFailureException {
        this.url = args.getUrl().endsWith("/") ? args.getUrl() : args.getUrl() + "/";
        this.secret = args.getSecret();

        try {
            // Try to make a connection
            HttpURLConnection connection = (HttpURLConnection) new URL(this.url).openConnection();
            connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(10));
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", this.secret);
            connection.getResponseCode();
        } catch (IOException error) {
            throw new ConnectionFailureException(error);
        }
    }

    @Override
    public void shutdown() {
        HTTP_CONNECTOR_EXECUTOR.shutdown();
    }

    @Override
    public void sendData(String channel, String argsSerialized) {
        HTTP_CONNECTOR_EXECUTOR.execute(() -> {
            try {
                JsonObject args = gson.fromJson(argsSerialized, JsonObject.class);

                String method = args.get("method").getAsString();
                String route = args.get("route").getAsString();
                String server = args.get("server").getAsString();

                long requestId = args.get("id").getAsLong();

                URL urlWithParams = new URL(this.url + (route.isEmpty() || route.endsWith("/") ? route : route + "/"));

                HttpURLConnection connection = (HttpURLConnection) urlWithParams.openConnection();
                connection.setRequestMethod(method);
                connection.setRequestProperty("Authorization", this.secret);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("X-Request-Id", requestId + "");
                connection.setRequestProperty("X-Server", server);
                connection.setInstanceFollowRedirects(true);

                if (args.has("body")) {
                    String body = args.get("body").getAsString();
                    connection.setDoOutput(true);

                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
                        writer.write(body);
                    }
                }

                StringBuilder body = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (body.length() > 0)
                            body.append("\n");
                        body.append(line);
                    }
                }

                notifyListeners(channel, body.toString());
            } catch (IOException error) {
                logger.warning("An error occurred while fetching response:");
                error.printStackTrace();
            }
        });
    }

}
