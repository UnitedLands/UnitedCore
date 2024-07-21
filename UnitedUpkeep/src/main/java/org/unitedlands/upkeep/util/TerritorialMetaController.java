package org.unitedlands.upkeep.util;

import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;

public class TerritorialMetaController {
    private static final BooleanDataField TERRITORIAL_WAR_KEY = new BooleanDataField("territorial_war ", true);


    public TerritorialMetaController() {
    }

    public static void toggleTerritorialWars(TownyObject obj) {
        BooleanDataField bdf = (BooleanDataField)TERRITORIAL_WAR_KEY.clone();
        if (obj.hasMeta(bdf.getKey())) {
            MetaDataUtil.setBoolean(obj, bdf, !MetaDataUtil.getBoolean(obj, bdf), true);
        } else {
            MetaDataUtil.addNewBooleanMeta(obj, bdf.getKey(), true, true);
        }

    }

    public static boolean toggledTerritorialWars(TownyObject obj) {
        BooleanDataField bdf = (BooleanDataField)TERRITORIAL_WAR_KEY.clone();
        return MetaDataUtil.getBoolean(obj, bdf);
    }
}
