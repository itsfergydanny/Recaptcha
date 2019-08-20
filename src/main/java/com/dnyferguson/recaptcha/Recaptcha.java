package com.dnyferguson.recaptcha;

import com.dnyferguson.recaptcha.commands.RecaptchaCommand;
import com.dnyferguson.recaptcha.listeners.*;
import com.dnyferguson.recaptcha.mysql.MySQL;
import com.dnyferguson.recaptcha.tasks.CheckPending;
import com.dnyferguson.recaptcha.utils.Utils;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Recaptcha extends JavaPlugin {

    private List<UUID> notPassed = new CopyOnWriteArrayList<>();
    private Map<UUID, String> urls = new HashMap<>();
    private MySQL sql;
    private CheckPending pendingCheck;
    private String denyMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        sql = new MySQL(this);

        StringBuilder message = new StringBuilder();
        for (String line : getConfig().getStringList("deny-message")) {
            message.append(Utils.color(line));
            message.append("\n");
        }
        denyMessage = message.toString();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new LoginListener(this), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new CommandListener(this), this);
        pm.registerEvents(new InteractionListener(this), this);
        pm.registerEvents(new InventoryOpenListener(this), this);
        pm.registerEvents(new InventoryClickListener(this), this);
        pm.registerEvents(new MoveListener(this), this);

        pendingCheck = new CheckPending(this);

        getCommand("recaptcha").setExecutor(new RecaptchaCommand(this));
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        if (sql != null) {
            sql.close();
        }
    }

    public List<UUID> getNotPassed() {
        return notPassed;
    }

    public MySQL getSql() {
        return sql;
    }

    public String getDenyMessage() {
        return denyMessage;
    }

    public Map<UUID, String> getUrls() {
        return urls;
    }
}
