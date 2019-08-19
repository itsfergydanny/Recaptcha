package com.dnyferguson.recaptcha.mysql;

import com.dnyferguson.recaptcha.Recaptcha;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQL {

    private Recaptcha plugin;
    private HikariDataSource ds;

    public MySQL(Recaptcha plugin) {
        this.plugin = plugin;
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("mysql");
        String db = config.getString("database");
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getString("ip") + ":" + config.getString("port") + "/" + db);
        hikariConfig.setUsername(config.getString("user"));
        hikariConfig.setPassword(config.getString("pass"));
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setMaximumPoolSize(config.getInt("connections"));

        ds = new HikariDataSource(hikariConfig);
        createTables(db);
    }

    private void createTables(String db) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try (Connection con = ds.getConnection()) {
                    PreparedStatement pst = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + db + "`.`users` ( `id` INT NOT NULL AUTO_INCREMENT , `ign` VARCHAR(16) NOT NULL , `uuid` VARCHAR(36) NOT NULL , `passed` BOOLEAN NOT NULL , `code` VARCHAR(100) NOT NULL , `completion_time` TIMESTAMP NULL DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE = InnoDB;");
                    pst.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public HikariDataSource getDs() {
        return ds;
    }

    public void close() {
        ds.close();
    }
}
