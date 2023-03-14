package org.unitedlands.wars;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.wars.commands.NationWarCommand;
import org.unitedlands.wars.commands.TownWarCommand;
import org.unitedlands.wars.commands.WarAdminCommand;
import org.unitedlands.wars.commands.mercenary.MercenaryCommand;
import org.unitedlands.wars.commands.surrender.SurrenderCommand;
import org.unitedlands.wars.hooks.Placeholders;
import org.unitedlands.wars.listeners.BookListener;
import org.unitedlands.wars.listeners.PlayerListener;
import org.unitedlands.wars.listeners.TownyListener;
import org.unitedlands.wars.listeners.WarListener;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDatabase;

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
        load();
        registerPlaceholders();
        displayWars();
    }

    private void registerListeners() {
        registerListener(new WarListener());
        registerListener(new PlayerListener(this));
        registerListener(new BookListener(this));
        registerListener(new TownyListener());
    }
    private void setCommandExecutors() {
        // Register war command for nations
        getCommand("nationwar").setExecutor(new NationWarCommand());
        // Register war command for towns
        getCommand("townwar").setExecutor(new TownWarCommand());
        getCommand("waradmin").setExecutor(new WarAdminCommand());
        getCommand("surrender").setExecutor(new SurrenderCommand());
        getCommand("mercenary").setExecutor(new MercenaryCommand());
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
        hideCurrentWars();
        WarDatabase.saveWarData();
        TownyCommandAddonAPI.removeSubCommand(TownyCommandAddonAPI.CommandType.TOWN, "war");
        TownyCommandAddonAPI.removeSubCommand(TownyCommandAddonAPI.CommandType.NATION, "war");
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

    private void hideCurrentWars() {
        for (War war: WarDatabase.getWars()) {
            war.getWarringEntities().forEach(entity -> {
                entity.getOnlinePlayers().forEach(p -> entity.getWarHealth().hide(p));
            });
        }
    }

    private void displayWars() {
        for (War war: WarDatabase.getWars()) {
            war.getWarringEntities().forEach(entity -> {
                entity.getOnlinePlayers().forEach(p -> entity.getWarHealth().show(p));
            });
        }
    }
}
