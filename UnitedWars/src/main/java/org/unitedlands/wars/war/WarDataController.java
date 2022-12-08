package org.unitedlands.wars.war;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;

public class WarDataController {
    private static final IntegerDataField residentLives = new IntegerDataField("unitedwars_residentLives", 0, "War Lives Remaining");
    private static final IntegerDataField tokens = new IntegerDataField("unitedwars_tokens", 0, "War Tokens");
    private static final LongDataField lastWarEndTime = new LongDataField("unitedwars_lastWarEndTime", 0L);

    public static int getResidentLives(Resident res) {
        IntegerDataField idf = (IntegerDataField) residentLives.clone();
        return res.hasMeta(idf.getKey()) ? MetaDataUtil.getInt(res, idf) : 0;
    }

    public static boolean hasResidentLives(Resident res) {
        return getResidentLives(res) > 0;
    }

    public static void setResidentLives(Resident res, int lives) {
        IntegerDataField idf = (IntegerDataField) residentLives.clone();
        if (res.hasMeta(idf.getKey())) {
            MetaDataUtil.setInt(res, idf, lives, true);
        } else {
            res.addMetaData(new IntegerDataField("unitedwars_residentLives", lives, "War Lives Remaining"), true);
        }
        res.save();
    }

    public static void decrementResidentLives(Resident res) {
        IntegerDataField idf = (IntegerDataField) residentLives.clone();
        if (res.hasMeta(idf.getKey())) {
            MetaDataUtil.setInt(res, idf, getResidentLives(res) - 1, true);
        }

        res.save();
    }

    public static void incrementResidentLives(Resident res) {
        IntegerDataField idf = (IntegerDataField) residentLives.clone();
        if (res.hasMeta(idf.getKey())) {
            MetaDataUtil.setInt(res, idf, getResidentLives(res) + 1, true);
        }

        res.save();
    }

    public static void removeResidentLivesMeta(Resident res) {
        IntegerDataField idf = (IntegerDataField) residentLives.clone();
        if (res.hasMeta(idf.getKey())) {
            res.removeMetaData(idf, true);
        }

    }

    public static long getLastWarTime(TownyObject obj) {
        LongDataField ldf = (LongDataField) lastWarEndTime.clone();
        return obj.hasMeta(ldf.getKey()) ? MetaDataUtil.getLong(obj, ldf) : 0L;
    }

    public static boolean hasLastWarTime(TownyObject obj) {
        LongDataField ldf = (LongDataField) lastWarEndTime.clone();
        return obj.hasMeta(ldf.getKey());
    }

    public static void setLastWarTime(TownyObject obj, long time) {
        LongDataField ldf = (LongDataField) lastWarEndTime.clone();
        if (obj.hasMeta(ldf.getKey())) {
            MetaDataUtil.setLong(obj, ldf, time, true);
        } else {
            MetaDataUtil.addNewLongMeta(obj, ldf.getKey(), time, true);
        }

    }

    public static void removeEndTime(TownyObject obj) {
        LongDataField ldf = (LongDataField) lastWarEndTime.clone();
        if (obj.hasMeta(ldf.getKey())) {
            obj.removeMetaData(ldf, true);
        }

    }

    public static int getWarTokens(TownyObject obj) {
        IntegerDataField idf = (IntegerDataField) tokens.clone();
        return obj.hasMeta(idf.getKey()) ? MetaDataUtil.getInt(obj, idf) : 0;
    }

    public static void setTokens(TownyObject obj, int num) {
        IntegerDataField idf = (IntegerDataField) tokens.clone();
        if (obj.hasMeta(idf.getKey())) {
            MetaDataUtil.setInt(obj, idf, num, true);
        } else {
            obj.addMetaData(new IntegerDataField("unitedwars_tokens", num, "War Tokens"), true);
        }

    }

    public static void removeTokens(TownyObject obj) {
        IntegerDataField idf = (IntegerDataField) tokens.clone();
        if (obj.hasMeta(idf.getKey())) {
            obj.removeMetaData(idf, true);
        }

    }

    public static void incrementTokens(TownyObject obj) {
        IntegerDataField idf = (IntegerDataField) tokens.clone();
        if (obj.hasMeta(idf.getKey())) {
            MetaDataUtil.setInt(obj, idf, getWarTokens(obj) + 1, true);
        } else {
            obj.addMetaData(new IntegerDataField("unitedwars_tokens", 1, "War Tokens"), true);
        }

    }
}
