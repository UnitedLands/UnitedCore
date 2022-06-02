package org.unitedlands.uniteditems;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.uniteditems.commands.TreeCmd;
import org.unitedlands.uniteditems.sapling.AncientOak;
import org.unitedlands.uniteditems.sapling.MidasJungle;
import org.unitedlands.uniteditems.sapling.MidasOak;
import org.unitedlands.uniteditems.sapling.FloweringAcacia;
import org.unitedlands.uniteditems.sapling.FungalSapling;
import org.unitedlands.uniteditems.sapling.MangoSapling;
import org.unitedlands.uniteditems.sapling.PineSapling;
import org.unitedlands.uniteditems.trees.Tree;
import org.unitedlands.uniteditems.util.SerializableData;

import java.io.File;

public class UnitedItems extends JavaPlugin {

    @Override
    public void onEnable() {

        this.getCommand("tree").setExecutor(new TreeCmd());
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
