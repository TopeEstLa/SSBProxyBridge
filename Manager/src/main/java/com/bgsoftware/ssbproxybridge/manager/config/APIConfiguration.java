package com.bgsoftware.ssbproxybridge.manager.config;

import com.bgsoftware.ssbproxybridge.manager.util.ConfigReader;
import com.bgsoftware.ssbproxybridge.manager.util.Resources;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class APIConfiguration {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    public final String serverAddress;
    public final int serverPort;

    public final long keepAlive;
    public final long inactiveTime;
    public final List<String> excludedServers;

    public APIConfiguration() throws IOException {
        File file = new File("config.yml");

        if (!file.exists())
            Resources.saveResource("config.yml", file);

        ConfigReader configReader = new ConfigReader(MAPPER.readValue(file, Map.class));

        this.serverAddress = configReader.get("server.address", "0.0.0.0");
        this.serverPort = configReader.get("server.port", 5000);
        this.keepAlive = TimeUnit.SECONDS.toMillis(configReader.get("keep-alive", 60));
        this.inactiveTime = TimeUnit.SECONDS.toMillis(configReader.get("inactive-time", 1209600));
        this.excludedServers = configReader.get("excluded-servers", Collections.singletonList("spawn"));
    }

}
