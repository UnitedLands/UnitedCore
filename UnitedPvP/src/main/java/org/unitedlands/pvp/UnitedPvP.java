package org.unitedlands.pvp;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.pvp.commands.PvPCmd;
import org.unitedlands.pvp.hooks.Placeholders;
import org.unitedlands.pvp.listeners.PlayerListener;
import org.unitedlands.pvp.listeners.TownyListener;
import org.unitedlands.pvp.util.OldTownBlock;
import org.unitedlands.pvp.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class UnitedPvP extends JavaPlugin {
    private ArrayList<OldTownBlock> oldTownBlocks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerListeners();
        loadTownBlocks();
    }

    public void onDisable() {
        saveTownBlocks();
    }

    private void registerListeners() {
        Utils utils = new Utils(this);
        PlayerListener playerListener = new PlayerListener(this, utils);
        Bukkit.getServer().getPluginManager().registerEvents(playerListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(new TownyListener(this), this);
        getCommand("pvp").setExecutor(new PvPCmd(utils));

        // PlaceholderAPI Expansion Register
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(utils).register();
        }

    }
    public ArrayList<OldTownBlock> getTownBlocksList() {
        return oldTownBlocks;
    }

    public void setOldTownBlocks(ArrayList<OldTownBlock> oldTownBlocks) {
        this.oldTownBlocks = oldTownBlocks;
    }

    private void loadTownBlocks() {
        File file = new File(this.getDataFolder(), "town-blocks.dat");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        ObjectInputStream input;
        try {
            input = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
            Object readObject = input.readObject();
            input.close();
            if(!(readObject instanceof ArrayList<?>)) {
                readObject = new ArrayList<OldTownBlock>();
            }
            oldTownBlocks = (ArrayList<OldTownBlock>) readObject;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void saveTownBlocks() {
        File file = new File(this.getDataFolder(), "town-blocks.dat");
        ObjectOutputStream output;
        try {
            output = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
            output.writeObject(oldTownBlocks);
            output.flush();
            output.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
