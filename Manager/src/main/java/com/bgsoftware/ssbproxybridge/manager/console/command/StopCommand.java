package com.bgsoftware.ssbproxybridge.manager.console.command;

import com.bgsoftware.ssbproxybridge.manager.Main;
import org.slf4j.Logger;

public class StopCommand implements ICommand {

    @Override
    public String usage() {
        return "stop";
    }

    @Override
    public String description() {
        return "Stop the manager.";
    }

    @Override
    public void run(Logger output, String[] args) {
        output.info("Stopping manager.");
        Main.safeShutdown();
    }

}
