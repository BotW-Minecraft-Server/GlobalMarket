package com.ender.globalmarket.storage;

import com.ender.globalmarket.Main;
import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigReader {

    public static FileConfiguration config = Main.instance.getConfig();

    public static String getMysqlConfig(String mysqlConfigTag) {
        return config.getString(mysqlConfigTag);
    }

    public static double getRate() {
        return config.getDouble("TaxRate");
    }

    public static int getTimeOut(String timeOutTag) {
        return config.getInt("TimeOut." + timeOutTag);
    }
    public static boolean getEnable(String enableTag) {
        return config.getBoolean("Enable." + enableTag);
    }

    public static double getTaxRate() {
        return config.getDouble("TaxRate");
    }


}