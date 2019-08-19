package com.dnyferguson.recaptcha.listeners;

import com.dnyferguson.recaptcha.Recaptcha;
import com.dnyferguson.recaptcha.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

    private Recaptcha plugin;
    private boolean enabled;

    public CommandListener(Recaptcha plugin) {
        this.plugin = plugin;
        enabled = plugin.getConfig().getBoolean("prevent-using-commands");
    }

    @EventHandler
    public void onPlayerRunCommand(PlayerCommandPreprocessEvent e) {
        if (!enabled) {
            return;
        }
        Player player = e.getPlayer();
        if (plugin.getNotPassed().contains(player.getUniqueId())) {
            e.setCancelled(true);
            player.sendMessage(Utils.color(plugin.getDenyMessage().replace("{url}", plugin.getUrls().get(player.getUniqueId()))));
        }
    }
}
