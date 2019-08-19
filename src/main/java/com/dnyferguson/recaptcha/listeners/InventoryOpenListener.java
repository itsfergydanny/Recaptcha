package com.dnyferguson.recaptcha.listeners;

import com.dnyferguson.recaptcha.Recaptcha;
import com.dnyferguson.recaptcha.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class InventoryOpenListener implements Listener {

    private Recaptcha plugin;
    private boolean enabled;

    public InventoryOpenListener(Recaptcha plugin) {
        this.plugin = plugin;
        enabled = plugin.getConfig().getBoolean("prevent-inventory-open");
    }

    @EventHandler
    public void onPlayerInventoryOpen(InventoryOpenEvent e) {
        if (!enabled) {
            return;
        }
        Player player = (Player) e.getPlayer();
        if (plugin.getNotPassed().contains(player.getUniqueId())) {
            e.setCancelled(true);
            player.sendMessage(Utils.color(plugin.getDenyMessage().replace("{url}", plugin.getUrls().get(player.getUniqueId()))));
        }
    }
}