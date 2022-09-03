package com.bgsoftware.ssbproxybridge.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        try {
            ManagerServer.getInstance().initialize();
        } catch (IOException | IllegalStateException error) {
            error.printStackTrace();
            return;
        }

        SpringApplication.run(Main.class, args);
    }

}
