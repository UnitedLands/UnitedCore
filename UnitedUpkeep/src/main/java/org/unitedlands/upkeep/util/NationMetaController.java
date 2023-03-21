package org.unitedlands.upkeep.util;

import co.aikar.util.LoadingMap;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;

public class NationMetaController {
    private static final BooleanDataField OFFICIAL_NATION_KEY = new BooleanDataField("official_nation", true);

    public static void setOfficialNation(TownyObject obj, boolean bool) {
        BooleanDataField bdf = (BooleanDataField) OFFICIAL_NATION_KEY.clone();
        if (obj.hasMeta(bdf.getKey())) {
            MetaDataUtil.setBoolean(obj, bdf, bool, true);
        } else {
            MetaDataUtil.addNewBooleanMeta(obj, bdf.getKey(), bool, true);
        }
    }
    public static boolean isOfficialNation(TownyObject obj) {
        BooleanDataField bdf = (BooleanDataField) OFFICIAL_NATION_KEY.clone();
        return MetaDataUtil.getBoolean(obj, bdf);
    }
}
