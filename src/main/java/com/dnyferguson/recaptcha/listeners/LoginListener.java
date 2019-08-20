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
import java.util.Random;

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
        String uuid = player.getUniqueId().toString();

        if (!plugin.getNotPassed().contains(player.getUniqueId())) {
            plugin.getNotPassed().add(player.getUniqueId());
        }

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try (Connection con = plugin.getSql().getDs().getConnection()) {
                    PreparedStatement pst = con.prepareStatement("SELECT * FROM `users` WHERE `uuid` = '" + uuid + "'");
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        boolean passed = rs.getBoolean("passed");
                        String code = rs.getString("code");
                        String fullUrl = url + "?code=" + code;
                        if (passed) {
                            plugin.getNotPassed().remove(player.getUniqueId());
                            return;
                        }
                        plugin.getUrls().put(player.getUniqueId(), fullUrl);
                        player.sendMessage(message.replace("{url}", url + "?code=" + code));
                        return;
                    }

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

                    String code = buffer.toString();
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
        }, delay);
    }
}
