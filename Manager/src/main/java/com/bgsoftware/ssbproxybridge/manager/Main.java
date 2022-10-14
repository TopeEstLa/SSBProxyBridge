package com.bgsoftware.ssbproxybridge.manager;

import com.bgsoftware.ssbproxybridge.manager.console.ConsoleReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class Main {

    private static ConfigurableApplicationContext context;
    private static boolean isRunning = true;

    public static void main(String[] args) {
        try {
            ManagerServer.getInstance().initialize();
        } catch (IOException | IllegalStateException error) {
            error.printStackTrace();
            return;
        }

        ConsoleReader consoleReader = new ConsoleReader();
        consoleReader.start();

        context = SpringApplication.run(Main.class, args);
    }

    public static void safeShutdown() {
        context.stop();
        isRunning = false;
    }

    public static boolean isRunning() {
        return isRunning;
    }
}
