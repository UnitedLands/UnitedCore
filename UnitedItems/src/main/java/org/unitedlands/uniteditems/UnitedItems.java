package org.unitedlands.uniteditems;

import org.unitedlands.uniteditems.commands.CustomItemCmd;
import org.unitedlands.uniteditems.commands.TreeCmd;
import org.unitedlands.uniteditems.listeners.EdibleListener;
import org.unitedlands.uniteditems.listeners.GUIListener;
import org.unitedlands.uniteditems.listeners.ItemListener;
import org.unitedlands.uniteditems.trees.Tree;
import org.unitedlands.uniteditems.util.SerializableData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.uniteditems.items.BracketMushroom;
import org.unitedlands.uniteditems.items.Croissant;
import org.unitedlands.uniteditems.items.Mango;
import org.unitedlands.uniteditems.items.Mimosa;
import org.unitedlands.uniteditems.items.Pinecone;
import org.unitedlands.uniteditems.sapling.AncientSeed;
import org.unitedlands.uniteditems.sapling.FloweringAcacia;
import org.unitedlands.uniteditems.sapling.FungalSapling;
import org.unitedlands.uniteditems.sapling.MangoSapling;
import org.unitedlands.uniteditems.sapling.PineSapling;

import java.io.File;

public class UnitedItems extends JavaPlugin {

    File customConfigFile;
    YamlConfiguration customConfig;

    @Override
    public void onEnable() {

        this.getCommand("customitem").setExecutor(new CustomItemCmd());
        this.getCommand("tree").setExecutor(new TreeCmd());
        Bukkit.getPluginManager().registerEvents(new EdibleListener(),this);
        getServer().getPluginManager().registerEvents(new ItemListener(), this);
        getServer().getPluginManager().registerEvents(new EdibleListener(), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new Tree(),this);

        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("UnitedCore").getDataFolder(), "messages.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();

            try {

                customConfigFile.createNewFile();
                customConfig = new YamlConfiguration();
                customConfig.load(customConfigFile);

            } catch (Exception e1){
                System.out.println("EXCEPTION: CANT CREATE NEW FILE OR LOAD IT");
            }
        }


        new Mango();
        new Croissant();
        new Pinecone();
        new BracketMushroom();
        new FungalSapling();
        new MangoSapling();
        new PineSapling();
        new AncientSeed();
        new FloweringAcacia();
        new Mimosa();

        Tree.load();

    }

    public static String getMsg(String s){
        File customConfigFile;
        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("UnitedCore").getDataFolder(),
                "messages.yml");
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
        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("UnitedCore").getDataFolder(),
                "messages.yml");
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
        SerializableData.Farming.writeToDatabase(Tree.getSerializableFruit(), "fruit.dat");
        SerializableData.Farming.writeToDatabase(Tree.getSerializableSaplings(), "sapling.dat");
        SerializableData.Farming.writeToDatabase(Tree.getSerializableLog(), "log.dat");
        // Plugin shutdown logic
    }

}
