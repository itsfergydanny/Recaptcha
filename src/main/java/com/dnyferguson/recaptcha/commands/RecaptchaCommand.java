package com.dnyferguson.recaptcha.commands;

import com.dnyferguson.recaptcha.Recaptcha;
import com.dnyferguson.recaptcha.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecaptchaCommand implements CommandExecutor {

    private Recaptcha plugin;

    public RecaptchaCommand(Recaptcha plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("recaptcha.help")) {
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(Utils.color("&cRecaptcha &7- Available Commands\n" +
                    "&c/recaptcha validate (ign/uuid) &7- Validate a user.\n" +
                    "&c/recaptcha invalidate (ign/uuid) &7- Invalidate a user.\n" +
                    "&c/recaptcha status (ign/uuid) &7- See a users status."));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "validate":
                if (!sender.hasPermission("recaptcha.validate")) {
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Utils.color("&cInvalid syntax. Use /recaptcha validate (ign/uuid)"));
                    return true;
                }

                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try (Connection con = plugin.getSql().getDs().getConnection()) {
                            String type = getType(args[1]);
                            PreparedStatement pst = con.prepareStatement("UPDATE `users` SET `passed`='1' WHERE `" + type + "` = '" + args[1] + "'");
                            pst.execute();
                            sender.sendMessage(Utils.color("&aYou have successfully validated the following user: " + args[1]));
                        } catch (SQLException e) {
                            sender.sendMessage(Utils.color("&cFailed to validate the specified user."));
                        }
                    }
                });
                break;
            case "invalidate":
                if (!sender.hasPermission("recaptcha.invalidate")) {
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Utils.color("&cInvalid syntax. Use /recaptcha invalidate (ign/uuid)"));
                    return true;
                }

                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try (Connection con = plugin.getSql().getDs().getConnection()) {
                            String type = getType(args[1]);
                            PreparedStatement pst = con.prepareStatement("UPDATE `users` SET `passed`='0', `code`='" + Utils.generateCode() + "' WHERE `" + type + "` = '" + args[1] + "'");
                            pst.execute();
                            sender.sendMessage(Utils.color("&aYou have successfully invalidated the following user: " + args[1]));
                        } catch (SQLException e) {
                            sender.sendMessage(Utils.color("&cFailed to invalidate the specified user."));
                        }
                    }
                });
                break;
            case "status":
                if (!sender.hasPermission("recaptcha.status")) {
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Utils.color("&cInvalid syntax. Use /recaptcha status (ign/uuid)"));
                    return true;
                }

                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try (Connection con = plugin.getSql().getDs().getConnection()) {
                            String type = getType(args[1]);
                            PreparedStatement pst = con.prepareStatement("SELECT * from `users` WHERE `" + type + "` = '" + args[1] + "'");
                            ResultSet rs = pst.executeQuery();
                            if (rs.next()) {
                                boolean passed = rs.getBoolean("passed");
                                if (passed) {
                                    sender.sendMessage(Utils.color("&auser ") + args[1] + " has passed their captcha.");
                                } else {
                                    sender.sendMessage(Utils.color("&cuser ") + args[1] + " has not passed their captcha.");
                                }
                            } else {
                                sender.sendMessage(Utils.color("&cFailed to find specified users."));
                            }
                        } catch (SQLException e) {
                            sender.sendMessage(Utils.color("&cFailed to check the specified user."));
                        }
                    }
                });
                break;
        }
        return false;
    }

    private String getType(String input) {
        if (input.contains("-")) {
            return "uuid";
        } else {
            return "ign";
        }
    }
}
