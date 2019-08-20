package com.dnyferguson.recaptcha.utils;

import org.bukkit.ChatColor;

import java.util.Random;

public class Utils {
    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
    public static String generateCode() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 12;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }

        return buffer.toString();
    }
}
