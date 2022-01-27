package com.tining.demonmarket;

import com.tining.demonmarket.command.UserCommand;
import com.tining.demonmarket.money.Vault;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Main extends JavaPlugin {
    public static JavaPlugin instance;
    private static final Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();


        if (Bukkit.getPluginCommand("mt") != null) {
            Bukkit.getPluginCommand("mt").setExecutor(new UserCommand());
        }

        if (Bukkit.getPluginCommand("demonmarket") != null) {
            Bukkit.getPluginCommand("demonmarket").setExecutor(new UserCommand());
        }

        Vault.vaultSetup();

    }
}