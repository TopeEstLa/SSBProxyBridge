package com.bgsoftware.ssbproxybridge.manager;

import com.bgsoftware.ssbproxybridge.manager.config.APIConfiguration;
import com.bgsoftware.ssbproxybridge.manager.tracker.ServersTracker;
import com.bgsoftware.ssbproxybridge.manager.util.SecretGenerator;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ManagerServer {

    private static final Logger logger = LoggerFactory.getLogger("ManagerServer");

    private static final ManagerServer INSTANCE = new ManagerServer();

    private final ServersTracker serversTracker = new ServersTracker(this);
    private APIConfiguration configuration;
    private String secret;

    private ManagerServer() {
    }

    public void initialize() throws IOException {
        if (this.configuration != null)
            throw new IllegalStateException("The manager was already been initialized.");

        this.configuration = new APIConfiguration();
        this.secret = readSecret();
    }

    public ServersTracker getServersTracker() {
        return serversTracker;
    }

    public APIConfiguration getConfig() {
        return configuration;
    }

    public String getSecret() {
        return this.secret;
    }

    public static ManagerServer getInstance() {
        return INSTANCE;
    }

    private static String readSecret() throws IOException {
        File file = new File("secret.json");

        if (!file.exists()) {
            logger.warn("Failed to find a valid secret, generating a new one.");
            // We want to generate a new strong secret
            String secret = SecretGenerator.generateSecret(20);
            ObjectWriter objectWriter = new ObjectMapper(new JsonFactory()).writerWithDefaultPrettyPrinter();
            Map<String, String> map = new HashMap<>();
            map.put("secret", secret);
            objectWriter.writeValue(file, map);
        }

        ObjectMapper secretReader = new ObjectMapper(new JsonFactory());
        Map<String, Object> content = secretReader.readValue(file, Map.class);

        Object secret = content.get("secret");

        if (!(secret instanceof String)) {
            file.delete();
            return readSecret();
        }

        return (String) secret;
    }

}
