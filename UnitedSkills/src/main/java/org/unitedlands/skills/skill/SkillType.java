package org.unitedlands.skills.skill;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum SkillType {
    // Brewer
    EXPOSURE_THERAPY("exposure-therapy", 3),
    ASSISTED_HEALING("assisted-healing", 2),
    MODIFIED_HARDWARE("modified-hardware", 3),
    BLEND("blend", 3),
    SPLASH_BOOST("splash-boost", 3),
    FORTIFICATION("fortification", 3),
    QUALITY_INGREDIENTS("quality-ingredients", 1),
    // Farmer
    GREEN_THUMB("green-thumb", 3),
    FERTILISER("fertiliser", 3),
    VEGETARIAN("vegetarian", 2),
    FUNGAL("fungal", 2),
    EXPERT_HARVESTER("expert-harvester", 3),
    // Miner
    FRENZY("frenzy", 3),
    FORTUNATE("fortunate", 3),
    BLAST_MINING("blast-mining", 3),
    PYROTECHNICS("pyrotechnics", 3),
    SHELL_SHOCKED("shell-shocked", 3),
    // Hunter
    SELF_REFLECTION("self-reflection", 3),
    RETRIEVER("retriever", 3),
    PIERCING("piercing", 3),
    CRITICAL_HIT("critical-hit", 3),
    PRECISION_STRIKE("precision-strike", 3),
    COUNTER_ATTACK("counter-attack", 3),
    STUN("stun", 3),
    FOCUS("focus", 3),
    // Fisher
    TREASURE_HUNTER("treasure-hunter", 3),
    ANGLER("angler", 3),
    LUCKY_CATCH("lucky-catch", 3),
    HOOKED("hooked", 3),
    PESCATARIAN("pescatarian", 2),
    GRAPPLE("grapple", 3),
    SWIFT_SWIMMER("swift-swimmer", 3),
    TREE_FELLER("tree-feller", 3),
    PRECISION_CUTTING("precision-cutting", 3),
    REFORESTATION("reforestation", 3);

    private final String name;

    private final int maxLevel;
    SkillType(String name, int maxLevel) {
        this.name = name;
        this.maxLevel = maxLevel;
    }
    public String getName() {
        return name;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}