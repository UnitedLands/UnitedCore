package org.unitedlands.skills.skill;

public enum SkillType {
    // Brewer
    EXPOSURE_THERAPY("exposure-therapy", 3),
    ASSISTED_HEALING("assisted-healing", 2),
    MODIFIED_HARDWARE("modified-hardware", 3),
    BLEND("blend", 3),
    SPLASH_BOOST("splash-boost", 3),
    FORTIFICATION("fortification", 3),
    QUALITY_INGREDIENTS("quality-ingredients", 1),
    OIL_EXTRACTION("oil-extraction", 1),
    NEW_POTIONS("new-potions", 3),
    // Farmer
    GREEN_THUMB("green-thumb", 3),
    FERTILISER("fertiliser", 3),
    VEGETARIAN("vegetarian", 2),
    FUNGAL("fungal", 2),
    WRANGLER("wrangler", 3),
    EXPERT_HARVESTER("expert-harvester", 3),
    ANCIENT_OAK("ancient-oak", 1),
    CARPENTER("carpenter", 3),
    // Miner
    FRENZY("frenzy", 3),
    FORTUNATE("fortunate", 3),
    BLAST_MINING("blast-mining", 3),
    PYROTECHNICS("pyrotechnics", 3),
    SHELL_SHOCKED("shell-shocked", 3),
    ARMOR_SMITH("armor-smith", 1),
    // Hunter
    SELF_REFLECTION("self-reflection", 3),
    RETRIEVER("retriever", 3, "Arrow Retrieved!"),
    PIERCING("piercing", 3, "Piercing Strike!"),
    CRITICAL_HIT("critical-hit", 3, "Critical Hit!"),
    PRECISION_STRIKE("precision-strike", 3),
    COUNTER_ATTACK("counter-attack", 3, "Attack Parried!"),
    STUN("stun", 3, "Enemy Stunned!"),
    FOCUS("focus", 3),
    TRAFFICKER("trafficker", 3),
    LEATHERWORKING("leatherworking", 1, "Dragon Scale collected!"),
    // Fisher
    TREASURE_HUNTER("treasure-hunter", 3),
    ANGLER("angler", 3),
    LUCKY_CATCH("lucky-catch", 3),
    HOOKED("hooked", 3),
    PESCATARIAN("pescatarian", 2),
    GRAPPLE("grapple", 3),
    RARE_CATCH("rare-catch", 3, "Rare fish caught!"),
    SWIFT_SWIMMER("swift-swimmer", 3),
    // Woodcutter
    TREE_FELLER("tree-feller", 3),
    PRECISION_CUTTING("precision-cutting", 3),
    REFORESTATION("reforestation", 3),
    EXPERT_WHITTLING("expert-whittling", 1),
    // Digger
    EXCAVATOR("excavator", 3),
    TUNNELLER("tunneller", 3),
    REFINER("refiner", 3),
    ARCHAEOLOGIST("archaeologist", 3),
    COMPACTOR("compactor", 3),
    BIOME_KIT("biome-kit", 1),
    MINERAL_FINDER("mineral-finder", 3, "Rare mineral found!");

    private final String name;

    private final int maxLevel;
    private final String activationMessage;

    SkillType(String name, int maxLevel) {
        this.name = name;
        this.maxLevel = maxLevel;
        activationMessage = null;
    }

    SkillType(String name, int maxLevel, String activationMessage) {
        this.name = name;
        this.maxLevel = maxLevel;
        this.activationMessage = activationMessage;
    }

    public String getName() {
        return name;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public String getActivationMessage() {
        return activationMessage;
    }
}