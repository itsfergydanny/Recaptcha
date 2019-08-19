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

    private Recaptcha plugin;
    private long time;
    private Iterator iterator;
    private String success;

    public CheckPending(Recaptcha plugin) {
        this.plugin = plugin;
        time = plugin.getConfig().getLong("validation-cycle-time");
        iterator = plugin.getNotPassed().iterator();
        success = plugin.getConfig().getString("success-message");
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if (iterator.hasNext()) {
                    UUID uuid = (UUID) iterator.next();
                    try (Connection con = plugin.getSql().getDs().getConnection()) {
                        PreparedStatement pst = con.prepareStatement("SELECT * FROM `users` WHERE `uuid` = '" + uuid + "'");
                        ResultSet rs = pst.executeQuery();
                        if (rs.next()) {
                            boolean passed = rs.getBoolean("passed");
                            if (passed) {
                                passed(uuid);
                            }
                        }
                    } catch (SQLException ignore) {}
                } else {
                    iterator = plugin.getNotPassed().iterator();
                }
            }
        }, 20, time);
    }

    private void passed(UUID uuid) {
        plugin.getNotPassed().remove(uuid);
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(Utils.color(success));
                }
            }
        });
    }
}
