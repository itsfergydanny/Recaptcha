package com.dnyferguson.recaptcha.listeners;

import com.dnyferguson.recaptcha.Recaptcha;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LogoutListener implements Listener {

    private Recaptcha plugin;

    public LogoutListener(Recaptcha plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        plugin.getNotPassed().remove(player.getUniqueId());
        plugin.getUrls().remove(player.getUniqueId());
    }
}
