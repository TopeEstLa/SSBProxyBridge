package com.bgsoftware.ssbproxybridge.manager.console.command;

import com.bgsoftware.ssbproxybridge.manager.console.ConsoleCommands;
import org.slf4j.Logger;

public class HelpCommand implements ICommand {

    @Override
    public String usage() {
        return "help";
    }

    @Override
    public String description() {
        return "List of available commands.";
    }

    @Override
    public void run(Logger output, String[] args) {
        for (ICommand command : ConsoleCommands.getInstance().getCommands()) {
            output.info("/" + command.usage() + ": " + command.description());
        }
    }

}
