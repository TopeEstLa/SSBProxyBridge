package com.bgsoftware.ssbproxybridge.core.http;

import com.bgsoftware.ssbproxybridge.core.Singleton;
import com.bgsoftware.ssbproxybridge.core.connector.ConnectionFailureException;
import com.bgsoftware.ssbproxybridge.core.connector.ConnectorAbstract;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class HttpConnector extends ConnectorAbstract<HttpConnectionArguments> {

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
    public void sendData(String channel, String data) {
        HTTP_CONNECTOR_EXECUTOR.execute(() -> {
            try {
                URL urlWithParams = new URL(this.url + data);

                HttpURLConnection connection = (HttpURLConnection) urlWithParams.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", this.secret);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                StringBuilder body = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
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
