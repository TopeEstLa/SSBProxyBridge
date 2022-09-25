package com.bgsoftware.ssbproxybridge.bukkit.utils;

import com.google.common.base.Preconditions;

import java.util.regex.Pattern;

public class PlayerLocales {

    private static final Pattern LOCALE_PATTERN = Pattern.compile("^[a-zA-Z]{2}[_|-][a-zA-Z]{2}$");

    private PlayerLocales() {
    }

    public static java.util.Locale getLocale(String str) throws IllegalArgumentException {
        str = str.replace("_", "-");

        Preconditions.checkArgument(LOCALE_PATTERN.matcher(str).matches(), "String " + str + " is not a valid language.");

        String[] numberFormatSections = str.split("-");

        return new java.util.Locale(numberFormatSections[0], numberFormatSections[1]);
    }

}
