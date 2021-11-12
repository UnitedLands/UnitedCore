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
import org.bukkit.plugin.java.JavaPlugin;

public final class ChromiumCustom extends JavaPlugin {

    @Override
    public void onEnable() {

        this.getCommand("customitem").setExecutor(new CustomItemCmd());
        this.getCommand("tree").setExecutor(new TreeCmd());
        Bukkit.getPluginManager().registerEvents(new EdibleListener(),this);
        getServer().getPluginManager().registerEvents(new ItemListener(), this);
        getServer().getPluginManager().registerEvents(new EdibleListener(), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new Tree(),this);


        new Mango();
        //new Croissant();
        new Pinecone();
        new BracketMushroom();
        new FungalSapling();
        new MangoSapling();
        new PineSapling();
        new AncientSeed();
        new Landscaper();
        new FloweringAcacia();
        new Mimosa();

        new BiomeSelector();

        Tree.load();

    }

    @Override
    public void onDisable() {
        SerializableData.Farming.writeToDatabase(Tree.getSerializableFruit(), "fruit.dat");
        SerializableData.Farming.writeToDatabase(Tree.getSerializableSaplings(), "sapling.dat");
        SerializableData.Farming.writeToDatabase(Tree.getSerializableLog(), "log.dat");
        // Plugin shutdown logic
    }

}
