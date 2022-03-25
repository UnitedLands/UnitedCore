package org.unitedlands.alcohol.brand;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.alcohol.UnitedBrands;
import org.unitedlands.alcohol.Util;

import java.util.ArrayList;
import java.util.List;

public class Brand {

    private final UnitedBrands ub;
    private final String name;
    private final Player owner;
    private final List<String> members;

    public Brand(UnitedBrands ub, String name, Player owner, List<String> members) {
        this.ub = ub;
        this.name = name;
        this.owner = owner;
        this.members = members;
    }

    public Player getBrandOwner() {
        return owner;
    }

    public String getBrandName() {
        return name;
    }

    public String getBrandSlogan() {
        FileConfiguration brandsConfig = getBrandsConfig();
        return brandsConfig.getString("brands." + name + ".slogan");
    }

    public void setSlogan(String slogan) {
        FileConfiguration brandsConfig = getBrandsConfig();
        brandsConfig.set("brands." + name + ".slogan", slogan);
        getBrandsFile().saveConfig(brandsConfig);
    }

    public void createBrand() {
        FileConfiguration brandsConfig = getBrandsConfig();
        brandsConfig.createSection("brands." + name);
        ConfigurationSection brandSection = brandsConfig.getConfigurationSection("brands." + name);
        brandSection.set("name", name);
        brandSection.set("owner-uuid", owner.getUniqueId().toString());
        brandSection.set("members", new ArrayList<String>());
        getBrandsFile().saveConfig(brandsConfig);

        owner.sendMessage(Util.getMessage("brand-created", name));
    }

    public void addMember(Player player) {
        FileConfiguration brandsConfig = getBrandsConfig();
        @NotNull List<String> members = getMembers();
        if (members == null) {
            members = brandsConfig.getStringList("brands." + name + ".members");
        }
        try {
            members.add(player.getUniqueId().toString());
            brandsConfig.set("brands." + name + ".members", members);
            getBrandsFile().saveConfig(brandsConfig);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteBrand() {
        FileConfiguration brandsConfig = getBrandsConfig();
        brandsConfig.set("brands." + name, null);
        getBrandsFile().saveConfig(brandsConfig);
    }

    private FileConfiguration getBrandsConfig() {
        return getBrandsFile().getBrandsConfig();
    }

    private BrandsFile getBrandsFile() {
        return new BrandsFile(ub);
    }


    public List<String> getMembers() {
        return members;
    }
}
