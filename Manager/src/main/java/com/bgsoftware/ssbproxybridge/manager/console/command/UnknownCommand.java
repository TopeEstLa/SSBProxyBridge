package com.bgsoftware.ssbproxybridge.manager.console.command;

import org.slf4j.Logger;

public class UnknownCommand implements ICommand {

    private static final UnknownCommand INSTANCE = new UnknownCommand();

    public static UnknownCommand getInstance() {
        return INSTANCE;
    }

    private UnknownCommand() {

    }

    @Override
    public String usage() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String description() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void run(Logger output, String[] args) {
        output.info("Unknown command. Type \"help\" for help.");
    }

}
