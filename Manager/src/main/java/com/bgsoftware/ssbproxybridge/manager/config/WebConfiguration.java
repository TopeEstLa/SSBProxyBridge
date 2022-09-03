package com.bgsoftware.ssbproxybridge.manager.config;

import com.bgsoftware.ssbproxybridge.manager.ManagerServer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

@org.springframework.context.annotation.Configuration
public class WebConfiguration implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private static final ManagerServer managerServer = ManagerServer.getInstance();

    public void customize(ConfigurableServletWebServerFactory factory) {
        try {
            factory.setAddress(InetAddress.getByName(managerServer.getConfig().serverAddress));
        } catch (UnknownHostException error) {
            error.printStackTrace();
        }
        factory.setPort(managerServer.getConfig().serverPort);
    }

}
