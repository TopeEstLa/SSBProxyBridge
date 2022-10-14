package com.bgsoftware.ssbproxybridge.manager.console.command;

import org.slf4j.Logger;

public interface ICommand {

    String usage();

    String description();

    void run(Logger output, String[] args);

}
