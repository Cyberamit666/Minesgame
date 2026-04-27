package com.minesgame;

import android.content.Context;

public class DatabaseHelper {
    private EconomyManager economy;

    public DatabaseHelper(Context context) {
        economy = new EconomyManager(context);
    }

    public EconomyManager getEconomy() {
        return economy;
    }
}