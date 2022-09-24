package com.bgsoftware.ssbproxybridge.bukkit.utils;

import com.bgsoftware.common.reflection.ReflectField;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Text {

    @Nullable
    private static final Pattern HEX_COLOR_PATTERN = createHexColorPattern();

    private Text() {

    }

    public static boolean isBlank(@Nullable String string) {
        return string == null || string.isEmpty();
    }

    public static String colorize(String text) {
        if (isBlank(text))
            return "";

        String output = ChatColor.translateAlternateColorCodes('&', text);

        if (HEX_COLOR_PATTERN == null)
            return output;

        while (true) {
            Matcher matcher = HEX_COLOR_PATTERN.matcher(output);

            if (!matcher.find())
                break;

            output = matcher.replaceFirst(parseHexColor(matcher.group(3)));
        }

        return output;
    }

    @Nullable
    private static Pattern createHexColorPattern() {
        ReflectField<Pattern> chatColorHexPatternField = new ReflectField<>(ChatColor.class, Pattern.class, "HEX_COLOR_PATTERN");
        return chatColorHexPatternField.isValid() ? Pattern.compile("([&§])(\\{HEX:([0-9A-Fa-f]*)})") : null;
    }

    private static String parseHexColor(String hexColor) {
        if (hexColor.length() != 6 && hexColor.length() != 3)
            return hexColor;

        StringBuilder magic = new StringBuilder(ChatColor.COLOR_CHAR + "x");
        int multiplier = hexColor.length() == 3 ? 2 : 1;

        for (char ch : hexColor.toCharArray()) {
            for (int i = 0; i < multiplier; i++)
                magic.append(ChatColor.COLOR_CHAR).append(ch);
        }

        return magic.toString();
    }

}
