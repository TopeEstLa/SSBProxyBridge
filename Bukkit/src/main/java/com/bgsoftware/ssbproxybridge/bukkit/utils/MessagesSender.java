package com.bgsoftware.ssbproxybridge.bukkit.utils;

import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import org.bukkit.command.CommandSender;

public class MessagesSender {

    private static boolean isSilentMessage = false;

    private MessagesSender() {

    }

    public static boolean isSilentMessage() {
        return isSilentMessage;
    }

    public static void sendMessageSilenty(IMessageComponent messageComponent, CommandSender target, Object... args) {
        try {
            isSilentMessage = true;
            messageComponent.sendMessage(target, args);
        } finally {
            isSilentMessage = false;
        }
    }

}
