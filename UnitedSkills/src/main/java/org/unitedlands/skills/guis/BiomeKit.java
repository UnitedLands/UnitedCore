package org.unitedlands.skills.guis;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import dev.lone.itemsadder.api.CustomStack;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.Utils;
import org.unitedlands.skills.skill.Skill;
import org.unitedlands.skills.skill.SkillType;

import java.util.ArrayList;
import java.util.List;

public class BiomeKit implements Listener {
    private final UnitedSkills unitedSkills;
    private Gui gui;
    private Player player;
    private ItemStack kit;

    public BiomeKit(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    @EventHandler
    public void onKitInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) {
            return;
        }
        if (!isBiomeKit(event.getItem())) {
            return;
        }
        kit = event.getItem();
        player = event.getPlayer();
        Skill skill = new Skill(player, SkillType.BIOME_KIT);
        if (skill.getLevel() == 0) {
            player.sendActionBar(Component.text("You need to unlock the Biome Kit skill in the digger job!", NamedTextColor.RED));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 1f);
            return;
        }
        if (event.getAction().isRightClick()) {
            Gui gui = createGUI(player);
            gui.open(player);
            return;
        }
        if (event.getAction().isLeftClick()) {
            if (canBuild()) {
                setChunkBiome(player.getChunk());
                playAnvilSound();
            } else {
                player.sendMessage(Utils.getMessage("must-have-build-permissions"));
            }
        }
    }

    public Gui createGUI(Player player) {
        this.player = player;
        gui = Gui.gui()
                .title(Component.text("Biome Kit",
                        NamedTextColor.RED, TextDecoration.BOLD))
                .rows(5)
                .create();
        addGuiPattern();
        gui.setOpenGuiAction(event -> playAnvilSound());
        setItems();
        return gui;
    }

    private void updateLore(Biome biome) {
        List<Component> lore = kit.lore();
        if (lore == null) return;

        Component updatedLoreLine = Component.text("Current Biome: ", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(getFormattedBiomeName(biome), NamedTextColor.GRAY));
        lore.set(lore.size() - 1, updatedLoreLine);
        kit.lore(lore);
    }

    private Biome getBiome() {
        NamespacedKey key = new NamespacedKey(unitedSkills, "biome");
        ItemMeta itemMeta = kit.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        if (container.has(key, PersistentDataType.STRING)) {
            String foundValue = container.get(key, PersistentDataType.STRING);
            return Biome.valueOf(foundValue);
        }
        return null;
    }

    private void setBiome(Biome biome) {
        NamespacedKey key = new NamespacedKey(unitedSkills, "biome");
        ItemMeta itemMeta = kit.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, biome.name().toUpperCase());
        kit.setItemMeta(itemMeta);
        updateLore(biome);
    }

    private void playAnvilSound() {
        player.playSound(player, Sound.BLOCK_ANVIL_USE, 1f, 1f);
    }

    private void addGuiPattern() {
        GuiItem redGlass = ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                .name(Component.text(" "))
                .asGuiItem(event -> event.setCancelled(true));
        GuiItem whiteGlass = ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE)
                .name(Component.text(" "))
                .asGuiItem(event -> event.setCancelled(true));
        List<GuiItem> glassList = new ArrayList<>();
        glassList.add(redGlass);
        glassList.add(whiteGlass);
        gui.getFiller().fillTop(glassList);
        gui.getFiller().fillBottom(glassList);
    }

    private void setItems() {
        FileConfiguration config = unitedSkills.getConfig();
        List<GuiItem> items = new ArrayList<>();
        List<String> biomes = getViableBiomes(config);
        if (biomes == null) {
            return;
        }
        for (String biomeInfo : biomes) {
            Biome extractedBiome = Biome.valueOf(biomeInfo.split(";")[0]);
            Material extractedMaterial = Material.valueOf(biomeInfo.split(";")[1]);
            String extractedHexColor = biomeInfo.split(";")[2];
            items.add(createBiomeItem(extractedMaterial, extractedBiome, extractedHexColor));
        }
        gui.getFiller().fill(items);
    }


    private List<String> getViableBiomes(FileConfiguration config) {
        String name = player.getWorld().getName();
        if (name.contains("end") || name.contains("nether")) {
            player.sendMessage(Component.text("You cannot use the biome kit in this world!").color(NamedTextColor.RED));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 1f);
            return null;
        } else {
            return config.getStringList("biome-kit.overworld-biomes");
        }
    }

    private GuiItem createBiomeItem(Material material, Biome biome, String hexColor) {
        return ItemBuilder.from(material)
                .name(Component
                        .text(getFormattedBiomeName(biome))
                        .color(TextColor.fromHexString(hexColor))
                        .decoration(TextDecoration.BOLD, true)
                        .decoration(TextDecoration.ITALIC, false))
                .lore(getBiomeItemLore(biome))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    setBiome(biome);
                    playAnvilSound();
                    player.sendMessage(Utils.getMessage("biome-selected"));
                    gui.close(player);
                });
    }

    private List<Component> getBiomeItemLore(Biome biome) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Click to change your chunk's ").color(NamedTextColor.GRAY));
        lore.add(Component.text("biome to ").color(NamedTextColor.GRAY)
                .append(Component.text(getFormattedBiomeName(biome) + "!").color(NamedTextColor.YELLOW)));
        lore.add(Component.text(""));
        lore.replaceAll(target -> target.decoration(TextDecoration.ITALIC, false));
        return lore;
    }

    private String getFormattedBiomeName(Biome biome) {
        return WordUtils.capitalize(biome.name().replace("_", " ").toLowerCase());
    }

    private boolean canBuild() {
        return PlayerCacheUtil.getCachePermission(player, player.getLocation(), Material.STONE, TownyPermission.ActionType.BUILD);
    }

    private boolean isBiomeKit(ItemStack item) {
        CustomStack biomeKit = CustomStack.getInstance("unitedlands:biome_kit");
        List<Component> targetLore = biomeKit.getItemStack().lore();

        List<Component> actualLore = item.lore();
        if (actualLore == null) {
            return false;
        }
        // Only compare the first line in the list, since the others are prone to change.
        return targetLore.get(0).equals(actualLore.get(0));
    }

    private void setChunkBiome(Chunk chunk) {
        Biome biome = getBiome();
        if (biome == null) {
            player.sendMessage(Utils.getMessage("choose-biome-first"));
            return;
        } else {
            player.sendMessage(Utils.getMessage("biome-changed"));
        }
        int cX = chunk.getX() * 16;
        int cZ = chunk.getZ() * 16;
        World world = chunk.getWorld();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y <= world.getMaxHeight(); y++) {
                    Block block = world.getBlockAt(x + cX, y, z + cZ);
                    block.setBiome(biome);
                }
            }
        }
        damageKit();
    }

    private void damageKit() {
        Damageable damageableKit = (Damageable) kit;
        damageableKit.setDamage(damageableKit.getDamage() + 1);
    }
}
