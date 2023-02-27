package org.unitedlands.items;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.items.commands.TreeCmd;
import org.unitedlands.items.listeners.FoodListener;
import org.unitedlands.items.sapling.AncientOak;
import org.unitedlands.items.sapling.MidasJungle;
import org.unitedlands.items.sapling.MidasOak;
import org.unitedlands.items.sapling.FloweringAcacia;
import org.unitedlands.items.sapling.FungalSapling;
import org.unitedlands.items.sapling.MangoSapling;
import org.unitedlands.items.sapling.PineSapling;
import org.unitedlands.items.trees.Tree;
import org.unitedlands.items.util.SerializableData;

import java.io.File;

public class UnitedItems extends JavaPlugin {

    @Override
    public void onEnable() {

        this.getCommand("tree").setExecutor(new TreeCmd());
        getServer().getPluginManager().registerEvents(new FoodListener(),this);
        getServer().getPluginManager().registerEvents(new Tree(this),this);
        saveDefaultConfig();

        new AncientOak();
        new FungalSapling();
        new MangoSapling();
        new PineSapling();
        new MidasOak();
        new MidasJungle();
        new FloweringAcacia();
        Tree.loadSaplings();

    }

    public static String getMsg(String s){
        File customConfigFile;
        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("UnitedItems").getDataFolder(),
                "config.yml");
        FileConfiguration customConfig;
        customConfig = new YamlConfiguration();
        try{
            customConfig.load(customConfigFile);
        } catch (Exception e2){
            System.out.println("Error with loading messages.");
        }

        return customConfig.getConfigurationSection("Items").getString(s);

    }

    public static String getGlobalMsg(String s){
        File customConfigFile;
        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("UnitedItems").getDataFolder(),
                "config.yml");
        FileConfiguration customConfig;
        customConfig = new YamlConfiguration();
        try{
            customConfig.load(customConfigFile);
        } catch (Exception e2){
            System.out.println("Error with loading messages.");
        }

        return customConfig.getConfigurationSection("Global").getString(s);

    }

    @Override
    public void onDisable() {
        SerializableData.Farming.writeToDatabase(Tree.getSerializableSaplings(), "sapling.dat");
    }

}
