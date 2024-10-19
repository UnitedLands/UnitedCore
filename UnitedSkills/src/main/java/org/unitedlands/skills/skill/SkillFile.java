package org.unitedlands.skills.skill;

import com.google.common.base.Charsets;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.UnitedSkills;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SkillFile {
    private final UnitedSkills unitedSkills;
    private FileConfiguration skillsConfig;

    public SkillFile(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    public void createSkillsFile() {
        File skillsFile = getSkillsFile();
        if (!skillsFile.exists()) {
            skillsFile.getParentFile().mkdirs();
            unitedSkills.saveResource("skills.yml", false);
        }
        skillsConfig = new YamlConfiguration();
        try {
            skillsConfig.load(skillsFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public  FileConfiguration getSkillsConfig() {
        File skillsFile = getSkillsFile();
        skillsConfig = new YamlConfiguration();
        try {
            skillsConfig.load(skillsFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return skillsConfig;
    }

    public void reloadConfig() {
        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(getSkillsFile());

        final InputStream defConfigStream = unitedSkills.getResource("skills.yml");
        if (defConfigStream == null) {
            return;
        }

        newConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
    }

    @NotNull
    private File getSkillsFile() {
        return new File(unitedSkills.getDataFolder(), "skills.yml");
    }
}
