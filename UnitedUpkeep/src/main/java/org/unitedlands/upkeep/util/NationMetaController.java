package org.unitedlands.upkeep.util;

import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;

public class NationMetaController {
    private static final BooleanDataField OFFICIAL_MAJOR_NATION_KEY = new BooleanDataField("official_major_nation", true);
    private static final BooleanDataField OFFICIAL_MINOR_NATION_KEY = new BooleanDataField("official_minor_nation", true);


    public NationMetaController() {
    }

    public static void setOfficialNation(TownyObject obj, boolean bool, String type) {
        if(type.equalsIgnoreCase("major")) {
            BooleanDataField bdf = (BooleanDataField)OFFICIAL_MAJOR_NATION_KEY.clone();
            if (obj.hasMeta(bdf.getKey())) {
                MetaDataUtil.setBoolean(obj, bdf, bool, true);
            } else {
                MetaDataUtil.addNewBooleanMeta(obj, bdf.getKey(), bool, true);
            }
        } else if(type.equalsIgnoreCase("minor")) {
            BooleanDataField bdf = (BooleanDataField)OFFICIAL_MINOR_NATION_KEY.clone();
            if (obj.hasMeta(bdf.getKey())) {
                MetaDataUtil.setBoolean(obj, bdf, bool, true);
            } else {
                MetaDataUtil.addNewBooleanMeta(obj, bdf.getKey(), bool, true);
            }
        }

    }

    public static boolean isOfficialNation(TownyObject obj, String type) {
        if(type.equalsIgnoreCase("major")) {
            BooleanDataField bdf = (BooleanDataField)OFFICIAL_MAJOR_NATION_KEY.clone();
            return MetaDataUtil.getBoolean(obj, bdf);
        } else if(type.equalsIgnoreCase("minor")) {
            BooleanDataField bdf = (BooleanDataField)OFFICIAL_MINOR_NATION_KEY.clone();
            return MetaDataUtil.getBoolean(obj, bdf);
        }
        return false;
    }
}
