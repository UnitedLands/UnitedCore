package org.unitedlands.skills.skill;

public enum SkillType {
    EXPOSURE_THERAPY("exposure-therapy", 3),
    ASSISTED_HEALING("assisted-healing", 2),
    MODIFIED_HARDWARE("modified-hardware", 3),
    BLEND("blend", 3),
    QUALITY_INGREDIENTS("quality-ingredients", 1),
    GREEN_THUMB("green-thumb", 3),
    FERTILISER("fertiliser", 3),
    VEGETARIAN("vegetarian", 2),
    FUNGAL("fungal", 2),
    EXPERT_HARVESTER("expert-harvester", 3),
    FRENZY("frenzy", 3),
    FORTUNATE("fortunate", 3),
    BLAST_MINING("blast-mining", 3),
    PYROTECHNICS("pyrotechnics", 3),
    SHELL_SHOCKED("shell-shocked", 3);

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