package com.dnyferguson.recaptcha.listeners;

import com.dnyferguson.recaptcha.Recaptcha;
import com.dnyferguson.recaptcha.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    private Recaptcha plugin;
    private boolean enabled;

    public MoveListener(Recaptcha plugin) {
        this.plugin = plugin;
        enabled = plugin.getConfig().getBoolean("prevent-moving");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!enabled) {
            return;
        }
        Player player = e.getPlayer();
        if (plugin.getNotPassed().contains(player.getUniqueId())) {
            Location from = e.getFrom();
            Location to = e.getTo();

            int fromX = (int) from.getX();
            int toX = (int) to.getX();

            if (fromX != toX) {
                e.setCancelled(true);
                try {
                    player.sendMessage(Utils.color(plugin.getDenyMessage().replace("{url}", plugin.getUrls().get(player.getUniqueId()))));
                } catch (Exception ignore) {}
                return;
            }

            int fromZ = (int) from.getZ();
            int toZ = (int) to.getZ();

            if (fromZ != toZ) {
                e.setCancelled(true);
                try {
                    player.sendMessage(Utils.color(plugin.getDenyMessage().replace("{url}", plugin.getUrls().get(player.getUniqueId()))));
                } catch (Exception ignore) {}
            }
        }
    }
}