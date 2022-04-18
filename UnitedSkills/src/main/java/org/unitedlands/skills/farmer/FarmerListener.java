package org.unitedlands.skills.farmer;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.UnitedSkills;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.unitedlands.skills.Utils.getSkillLevel;
import static org.unitedlands.skills.Utils.isSuccessful;

public class FarmerListener implements Listener {

    private final UnitedSkills unitedSkills;
    private Player player;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Long> activeDurations = new HashMap<>();

    public FarmerListener(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    @EventHandler
    public void onInteractWithHoe(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (!isFarmer()) {
            return;
        }
        int skillLevel = getSkillLevel(player, "green-thumb");
        if (skillLevel == 0) {
            return;
        }
        if (!player.getInventory().getItemInMainHand().getType().toString().contains("HOE")) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }
        final UUID uuid = player.getUniqueId();
        int cooldownTime = getConfig().getInt("cooldowns." + ".green-thumb." + skillLevel);
        int durationTime = getConfig().getInt("durations." + ".green-thumb." + skillLevel);
        if (greenThumbIsActive(uuid)) {
            long secondsLeft = (activeDurations.get(uuid) - System.currentTimeMillis()) / 1000;
            player.sendActionBar(Component
                    .text("Green Thumb active for " + secondsLeft, NamedTextColor.RED));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 1f);
            return;
        }
        if (isOnCooldown(uuid)) {
            long secondsLeft = (cooldowns.get(uuid) - System.currentTimeMillis()) / 1000;
            player.sendActionBar(Component
                    .text("You can activate this skill in "
                            + secondsLeft + "s", NamedTextColor.RED));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 1f);
            return;
        }
        notifySkillActivation("Green Thumb");
        addTime(durationTime, activeDurations);
        addTime(cooldownTime, cooldowns);
    }

    private void addTime(int cooldownTime, HashMap<UUID, Long> map) {
        @NotNull UUID uuid = player.getUniqueId();
        map.put(uuid, System.currentTimeMillis() + (cooldownTime * 1000L));
    }

    private boolean isOnCooldown(UUID uuid) {
        return cooldowns.containsKey(uuid) && cooldowns.get(uuid) > System.currentTimeMillis();
    }

    private boolean greenThumbIsActive(UUID uuid) {
        return activeDurations.containsKey(uuid) && activeDurations.get(uuid) > System.currentTimeMillis();
    }

    @EventHandler
    public void onCropPlant(BlockPlaceEvent event) {
        player = event.getPlayer();
        if (!isFarmer()) {
            return;
        }
        int skillLevel = getSkillLevel(player, "fertiliser");
        if (skillLevel == 0) {
            return;
        }
        Block block = event.getBlock();
        if (!isCrop(block.getType())) {
            return;
        }
        Ageable crop = (Ageable) block.getBlockData();
        if (isSuccessful(player, "fertiliser")) {
            int currentAge = crop.getAge();
            crop.setAge(currentAge + skillLevel * 2 + 1);
            block.setBlockData(crop);
            notifySkillActivation("Fertiliser");
        }

    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        player = event.getPlayer();
        if (!isFarmer()) {
            return;
        }
        int skillLevel = getSkillLevel(player, "fungal");
        if (skillLevel < 2) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Cow)) {
            return;
        }
        ItemStack offhandItem = player.getInventory().getItemInOffHand();
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (isHoldingMushrooms(handItem, offhandItem)) {
            Location location = entity.getLocation();
            entity.remove();
            entity.getWorld().spawnEntity(location, EntityType.MUSHROOM_COW);
            runFungalSkill(offhandItem, handItem);
        }
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (!isFarmer()) {
            return;
        }
        int skillLevel = getSkillLevel(player, "fungal");
        if (skillLevel == 0) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        ItemStack offhandItem = player.getInventory().getItemInOffHand();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (block.getType().equals(Material.GRASS_BLOCK) || block.getType().equals(Material.DIRT)) {
            if (isHoldingMushrooms(handItem, offhandItem)) {
                event.setCancelled(true);
                block.setType(Material.MYCELIUM);
                runFungalSkill(offhandItem, handItem);
            }
        }
    }

    private void runFungalSkill(ItemStack offhandItem, ItemStack handItem) {
        if (offhandItem.getAmount() > 1) {
            offhandItem.setAmount(offhandItem.getAmount() - 1);
            player.getInventory().setItemInOffHand(offhandItem);
        } else {
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        }
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            player.getInventory().remove(handItem);
        }
        notifySkillActivation("Fungal");
    }

    private void notifySkillActivation(String name) {
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        player.sendActionBar(Component.text(name + " Skill Activated!", NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true));
    }

    private boolean isHoldingMushrooms(ItemStack handItem, ItemStack offhandItem ) {
        final Material offhandItemType = offhandItem.getType();
        final Material handItemType = handItem.getType();
        if (offhandItemType.equals(Material.RED_MUSHROOM) && handItemType.equals(Material.BROWN_MUSHROOM)) {
            return true;
        } else if (offhandItemType.equals(Material.BROWN_MUSHROOM) && handItemType.equals(Material.RED_MUSHROOM)) {
            return true;
        }
        return false;
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        player = event.getPlayer();
        if (!isPlantFood(item.getType())) {
            return;
        }
        if (!isFarmer()) {
            return;
        }
        int skillLevel = getSkillLevel(player, "vegetarian");
        if (skillLevel == 0) {
            return;
        }
        float saturation = player.getSaturation();
        int foodLevel = player.getFoodLevel();
        player.setSaturation(saturation + skillLevel);
        player.setFoodLevel(foodLevel + skillLevel);
    }

    @EventHandler
    public void onCropBreak(BlockBreakEvent event) {
        Material material = event.getBlock().getType();
        player = event.getPlayer();
        if (!isCrop(material)) {
            return;
        }
        if (!isFarmer()) {
            return;
        }
        int skillLevel = getSkillLevel(player, "green-thumb");
        if (skillLevel == 0) {
            return;
        }
        Block block = event.getBlock();
        BlockData dataPlant = block.getBlockData();
        if (!(dataPlant instanceof Ageable))  {
            return;
        }
        Ageable plant = (Ageable) dataPlant;
        if (isSuccessful(player, "green-thumb") && greenThumbIsActive(player.getUniqueId())) {
            if (takeSeeds(player, material)) {
                unitedSkills.getServer().getScheduler().runTask(unitedSkills, () -> {
                    block.setType(plant.getMaterial());
                    plant.setAge(0);
                    block.setBlockData(plant);
                });
            }
        }
    }

    private Material getCropSeeds(@NotNull Material material) {
        if (material == Material.POTATOES) return Material.POTATO;
        if (material == Material.CARROTS) return Material.CARROT;
        if (material == Material.BEETROOTS) return Material.BEETROOT_SEEDS;
        if (material == Material.WHEAT) return Material.WHEAT_SEEDS;
        if (material == Material.MELON_STEM) return Material.MELON_SEEDS;
        if (material == Material.PUMPKIN_STEM) return Material.PUMPKIN_SEEDS;
        return material;
    }

    private boolean takeSeeds(@NotNull Player player, @NotNull Material material) {
        material = getCropSeeds(material);

        int slot = player.getInventory().first(material);
        if (slot < 0) return false;

        ItemStack seed = player.getInventory().getItem(slot);
        if (seed == null || seed.getType().isAir()) return false;

        seed.setAmount(seed.getAmount() - 1);
        return true;
    }

    @EventHandler
    public void onCropDrop(BlockDropItemEvent event) {
        @NotNull Material material = event.getBlockState().getType();
        player = event.getPlayer();
        if (!isCrop(material)) {
            return;
        }
        if (!isFarmer()) {
            return;
        }
        int skillLevel = getSkillLevel(player, "expert-harvester");
        if (skillLevel == 0) {
            return;
        }
        if (isSuccessful(player, "green-thumb")) {
            for (Item item : event.getItems())
            if (greenThumbIsActive(player.getUniqueId())) {
                player.getInventory().addItem(item.getItemStack());
                player.getInventory().addItem(item.getItemStack());
            }
        }

        if (isSuccessful(player, "expert-harvester")) {
            for (Item item : event.getItems()) {
                if (item.getName().contains("Seeds")) {
                    return;
                }
                player.getInventory().addItem(item.getItemStack());
            }
        }
    }

    private boolean isCrop(@NotNull Material material) {
        FileConfiguration configuration = getConfig();
        List<String> cropNames = configuration.getStringList("crop-names");
        return cropNames.contains(material.toString());
    }

    private boolean isPlantFood(Material material) {
        FileConfiguration configuration = getConfig();
        return configuration.getStringList("plant-foods").contains(material.toString());
    }

    private boolean isFarmer() {
        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        for (JobProgression job : jobsPlayer.getJobProgression()) {
            return job.getJob().getName().equals("Farmer");
        }
        return false;
    }
    @NotNull
    private FileConfiguration getConfig() {
        return unitedSkills.getConfig();
    }

}
