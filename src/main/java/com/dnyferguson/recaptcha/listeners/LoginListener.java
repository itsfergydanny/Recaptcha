package com.dnyferguson.recaptcha.listeners;

import com.dnyferguson.recaptcha.Recaptcha;
import com.dnyferguson.recaptcha.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginListener implements Listener {

    private Recaptcha plugin;
    private String message;
    private String url;
    private long delay;

    public LoginListener(Recaptcha plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        StringBuilder message = new StringBuilder();
        for (String line : config.getStringList("message")) {
            message.append(Utils.color(line));
            message.append("\n");
        }
        this.message = message.toString();
        url = config.getString("url");
        delay = config.getLong("message-delay");
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                check(player);
            }
        }, delay);
    }

    public void check(Player player) {
        String uuid = player.getUniqueId().toString();
        try (Connection con = plugin.getSql().getDs().getConnection()) {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM `users` WHERE `uuid` = '" + uuid + "'");
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                boolean passed = rs.getBoolean("passed");
                String code = rs.getString("code");
                String fullUrl = url + "?code=" + code;
                if (passed) {
                    return;
                }
                if (!plugin.getNotPassed().contains(player.getUniqueId())) {
                    plugin.getNotPassed().add(player.getUniqueId());
                }
                plugin.getUrls().put(player.getUniqueId(), fullUrl);
                player.sendMessage(message.replace("{url}", url + "?code=" + code));
                System.out.println("[Recaptcha] generated captcha for new user " + player.getName() + ".");
                return;
            }

            String code = Utils.generateCode();
            String fullUrl = url + "?code=" + code;
            plugin.getUrls().put(player.getUniqueId(), fullUrl);

            pst = con.prepareStatement("INSERT INTO `users` (`id`, `ign`, `uuid`, `passed`, `code`, `completion_time`) VALUES " +
                    "(NULL, '" + player.getName() + "', '" + uuid + "', '0', '" + code + "', NULL)");
            pst.execute();
            player.sendMessage(message.replace("{url}", fullUrl));
        } catch (SQLException ignore) {
            plugin.getNotPassed().remove(player.getUniqueId());
        }
    }
}
