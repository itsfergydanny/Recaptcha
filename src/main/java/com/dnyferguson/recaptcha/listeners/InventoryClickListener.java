package com.dnyferguson.recaptcha.listeners;

import com.dnyferguson.recaptcha.Recaptcha;
import com.dnyferguson.recaptcha.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    private Recaptcha plugin;
    private boolean enabled;

    public InventoryClickListener(Recaptcha plugin) {
        this.plugin = plugin;
        enabled = plugin.getConfig().getBoolean("prevent-inventory-click");
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent e) {
        if (!enabled) {
            return;
        }
        Player player = (Player) e.getWhoClicked();
        if (plugin.getNotPassed().contains(player.getUniqueId())) {
            e.setCancelled(true);
            player.sendMessage(Utils.color(plugin.getDenyMessage().replace("{url}", plugin.getUrls().get(player.getUniqueId()))));
        }
    }
}