package com.minesgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameLogic {
    private final int rows = 5;
    private final int cols = 5;
    private final int totalTiles = 25;
    private List<Boolean> isMine; // true = mine
    private boolean[] revealed;
    private int numMines;
    private double multiplier = 1.0;
    private int safeClicked = 0;
    private double currentWinnings;
    private double bet;

    public GameLogic(double betAmount, int minesCount) {
        this.bet = betAmount;
        this.currentWinnings = betAmount;
        this.numMines = Math.max(1, Math.min(minesCount, 24));
        resetBoard();
    }

    private void resetBoard() {
        isMine = new ArrayList<>();
        for (int i = 0; i < totalTiles; i++) isMine.add(false);
        
        // Place mines randomly
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < totalTiles; i++) positions.add(i);
        Collections.shuffle(positions);
        for (int i = 0; i < numMines; i++) {
            isMine.set(positions.get(i), true);
        }
        
        revealed = new boolean[totalTiles];
        multiplier = 1.0;
        safeClicked = 0;
    }

    // Returns true if safe, false if mine hit
    public boolean clickTile(int index) {
        if (revealed[index]) return true;
        revealed[index] = true;
        
        if (isMine.get(index)) {
            return false; // boom
        }
        
        safeClicked++;
        // Progressive multiplier (higher risk = bigger growth)
        multiplier = 1.0 + (safeClicked * 0.35) * (numMines / 8.0);
        currentWinnings = bet * multiplier;
        return true;
    }

    public double cashOut() {
        return currentWinnings;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public double getCurrentWinnings() {
        return currentWinnings;
    }

    public boolean isRevealed(int index) {
        return revealed[index];
    }

    public boolean isMineTile(int index) {
        return isMine.get(index);
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
}