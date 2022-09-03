package com.bgsoftware.ssbproxybridge.manager.util;

import org.springframework.lang.Nullable;

import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})

public class ConfigReader {

    private final Map<String, Object> configMap;

    public ConfigReader(Map configMap) {
        this.configMap = configMap;
    }

    @Nullable
    public <T> T get(String path) {
        String[] subPaths = path.split("\\.");

        Map<String, Object> currentMap = configMap;

        for (int i = 0; i < subPaths.length - 1; ++i) {
            Object val = currentMap.get(subPaths[i]);
            if (!(val instanceof Map))
                return null;
            currentMap = (Map<String, Object>) val;
        }

        return (T) currentMap.get(subPaths[subPaths.length - 1]);
    }

    public <T> T get(String path, T def) {
        T val = get(path);
        return val == null ? def : val;
    }

}
