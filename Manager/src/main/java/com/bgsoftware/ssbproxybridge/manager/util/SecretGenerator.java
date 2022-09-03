package com.bgsoftware.ssbproxybridge.manager.util;

import java.security.SecureRandom;

public class SecretGenerator {

    private static final char[] ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private SecretGenerator() {

    }

    public static String generateSecret(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder secret = new StringBuilder();

        for (int i = 0; i < length; ++i) {
            secret.append(ALLOWED_CHARS[random.nextInt(ALLOWED_CHARS.length)]);
        }

        return secret.toString();
    }

}
