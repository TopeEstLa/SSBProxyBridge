package com.bgsoftware.ssbproxybridge.manager.console;

import com.bgsoftware.ssbproxybridge.manager.Main;
import com.bgsoftware.ssbproxybridge.manager.console.command.ICommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Scanner;

public class ConsoleReader extends Thread {

    private static final Logger logger = LoggerFactory.getLogger("Manager");

    public ConsoleReader() {
        setName("ConsoleReader");
    }

    @Override
    public void run() {
        try {
            System.in.available();
        } catch (IOException ex) {
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            while (Main.isRunning()) {
                String[] userInput = scanner.next().split(" ");
                String commandLabel = userInput[0];
                String[] args = new String[userInput.length - 1];

                if (args.length > 0)
                    System.arraycopy(userInput, 1, args, 0, args.length);

                ICommand command = ConsoleCommands.getInstance().getCommand(commandLabel);
                command.run(logger, args);
            }
        }
    }

}
