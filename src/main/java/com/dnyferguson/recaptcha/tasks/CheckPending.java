package com.dnyferguson.recaptcha.tasks;

import com.dnyferguson.recaptcha.Recaptcha;
import com.dnyferguson.recaptcha.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.UUID;

public class CheckPending {

    private Iterator iterator;
    private String success;

    public CheckPending(Recaptcha plugin) {
        int time = plugin.getConfig().getInt("validation-cycle-time");
        iterator = plugin.getNotPassed().iterator();
        success = plugin.getConfig().getString("success-message");
        plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (iterator.hasNext()) {
                    UUID uuid = (UUID) iterator.next();
//                    System.out.println("[Debug] iteration, uuid: " + uuid);
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
//                        System.out.println("[Debug] " + uuid + " is no longer online, removing from captcha collections.");
                        plugin.getNotPassed().remove(uuid);
                        plugin.getUrls().remove(uuid);
                        return;
                    }
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                        @Override
                        public void run() {
                            try (Connection con = plugin.getSql().getDs().getConnection()) {
                                PreparedStatement pst = con.prepareStatement("SELECT * FROM `users` WHERE `uuid` = '" + uuid + "'");
                                ResultSet rs = pst.executeQuery();
                                if (rs.next()) {
                                    boolean passed = rs.getBoolean("passed");
                                    String ign = rs.getString("ign");
                                    if (passed) {
                                        plugin.getNotPassed().remove(uuid);
                                        plugin.getUrls().remove(uuid);
                                        player.sendMessage(Utils.color(success));
                                        System.out.println("[Recaptcha] user " + ign + " has passed their captcha! They can now play!");
                                    }
                                }
                            } catch (SQLException ignore) {}
                        }
                    });
                } else {
                    iterator = plugin.getNotPassed().iterator();
                }
            }
        }, 0, time);
    }
}
