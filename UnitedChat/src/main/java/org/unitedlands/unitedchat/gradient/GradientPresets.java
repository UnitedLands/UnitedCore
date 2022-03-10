package org.unitedlands.unitedchat.gradient;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;

public class GradientPresets {

        private static HashMap<String, Gradient> gradients = new HashMap<String, Gradient>();

        @SuppressWarnings("unchecked")
        public static void loadPredefinedGradients(FileConfiguration cfg) {
            String path = "Preset Gradients";

            for (String key : cfg.getConfigurationSection(path).getKeys(false)) {
                //System.out.println(key);
                gradients.put(key, new Gradient((ArrayList<String>) cfg.getList(path+"."+key)));
            }

        }

        public static Gradient getGradient(String name) {

                if(gradients.containsKey(name.toLowerCase())){
                    return gradients.get(name);
                } else {
                    return null;
                }


        }

}
