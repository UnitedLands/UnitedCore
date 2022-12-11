package org.unitedlands.wars;

import com.palmergames.bukkit.towny.TownyAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.wars.commands.WarAdminCommand;
import org.unitedlands.wars.commands.WarCommand;
import org.unitedlands.wars.commands.surrender.SurrenderCommand;
import org.unitedlands.wars.hooks.Placeholders;
import org.unitedlands.wars.listeners.BookListener;
import org.unitedlands.wars.listeners.PlayerListener;
import org.unitedlands.wars.listeners.WarListener;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.health.WarHealTask;

import java.io.File;
import java.io.IOException;

public final class UnitedWars extends JavaPlugin {
    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    public static final TownyAPI TOWNY_API = TownyAPI.getInstance();
    private static UnitedWars plugin;

    private FileConfiguration warConfig;

    public UnitedWars() {
        plugin = this;
    }

    public static UnitedWars getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        registerListeners();
        setCommandExecutors();
        runTasks();
        load();
        registerPlaceholders();
    }

    private void registerListeners() {
        registerListener(new WarListener());
        registerListener(new PlayerListener(this));
        registerListener(new BookListener(this));
    }
    private void setCommandExecutors() {
        getCommand("war").setExecutor(new WarCommand());
        getCommand("waradmin").setExecutor(new WarAdminCommand());
        getCommand("surrender").setExecutor(new SurrenderCommand());
    }
    private void runTasks() {
        WarHealTask warHealTask = new WarHealTask();
        warHealTask.runTaskTimer(this, 0, 60 * 20 * 20);
    }

    private void load() {
        createWarConfig();
        WarDatabase.loadSavedData();
        saveDefaultConfig();
    }

    private void registerPlaceholders() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new Placeholders().register();
        }
    }
    @Override
    public void onDisable() {
        WarDatabase.saveWarData();
    }

    public FileConfiguration getWarConfig() {
        return this.warConfig;
    }

    private void createWarConfig() {
        File warFile = new File(getDataFolder(), "wars.yml");
        if (!warFile.exists()) {
            warFile.getParentFile().mkdirs();
            saveResource("wars.yml", false);
        }

        warConfig = new YamlConfiguration();
        try {
            warConfig.load(warFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }


    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }
}
