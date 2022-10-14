package com.bgsoftware.ssbproxybridge.manager.console;

import com.bgsoftware.ssbproxybridge.manager.console.command.HelpCommand;
import com.bgsoftware.ssbproxybridge.manager.console.command.ICommand;
import com.bgsoftware.ssbproxybridge.manager.console.command.StopCommand;
import com.bgsoftware.ssbproxybridge.manager.console.command.UnknownCommand;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ConsoleCommands {

    private static final ConsoleCommands INSTANCE = new ConsoleCommands();

    public static ConsoleCommands getInstance() {
        return INSTANCE;
    }

    private static final Map<String, ICommand> COMMANDS_MAP = new LinkedHashMap<>();

    static {
        COMMANDS_MAP.put("help", new HelpCommand());
        COMMANDS_MAP.put("stop", new StopCommand());
    }

    private ConsoleCommands() {

    }

    public ICommand getCommand(String label) {
        return COMMANDS_MAP.getOrDefault(label.toLowerCase(Locale.ENGLISH), UnknownCommand.getInstance());
    }

    public Collection<ICommand> getCommands() {
        return Collections.unmodifiableCollection(COMMANDS_MAP.values());
    }

}
