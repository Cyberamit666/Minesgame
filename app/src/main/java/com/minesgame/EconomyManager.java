package com.minesgame;

import android.content.Context;
import android.content.SharedPreferences;

public class EconomyManager {
    private static final String PREF_NAME = "MinesGamePrefs";
    private static final String KEY_BALANCE = "balance";
    private SharedPreferences prefs;

    public EconomyManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (getBalance() == 0) setBalance(10000);
    }

    public long getBalance() {
        return prefs.getLong(KEY_BALANCE, 10000);
    }

    public void setBalance(long newBalance) {
        prefs.edit().putLong(KEY_BALANCE, newBalance).apply();
    }

    public boolean placeBet(long bet) {
        long bal = getBalance();
        if (bet > bal || bet < 10) return false;
        setBalance(bal - bet);
        return true;
    }

    public void addWinnings(long winnings) {
        setBalance(getBalance() + winnings);
    }
}