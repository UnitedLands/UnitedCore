package me.obito.chromiumcustom;

import me.obito.chromiumcustom.commands.CustomItemCmd;
import me.obito.chromiumcustom.commands.TreeCmd;
import me.obito.chromiumcustom.items.*;
import me.obito.chromiumcustom.items.weapon.Landscaper;
import me.obito.chromiumcustom.listeners.EdibleListener;
import me.obito.chromiumcustom.listeners.GUIListener;
import me.obito.chromiumcustom.listeners.ItemListener;
import me.obito.chromiumcustom.sapling.*;
import me.obito.chromiumcustom.trees.Tree;
import me.obito.chromiumcustom.util.BiomeSelector;
import me.obito.chromiumcustom.util.SerializableData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ChromiumItems extends JavaPlugin {

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

        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(), "messages.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();

            try {

                customConfigFile.createNewFile();
                customConfig = new YamlConfiguration();
                customConfig.load(customConfigFile);

            } catch (Exception e1){
                System.out.println("EXCEPTION: CANT CREATE NEW FILE OR LOAD IT");
            }
            //Bukkit.getPluginManager().getPlugin("ChromiumCore").saveResource(e.getPlayer().getUniqueId() + ".yml", false);
        }


        new Mango();
        new Croissant();
        new Pinecone();
        new BracketMushroom();
        new FungalSapling();
        new MangoSapling();
        new PineSapling();
        new AncientSeed();
        new Landscaper();
        new FloweringAcacia();
        new Mimosa();
        new DarkMagic();
        new Sausage();

        new BiomeSelector();

        Tree.load();

    }

    public static String getMsg(String s){
        File customConfigFile;
        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
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
        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
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
