package com.minesgame;

import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import com.minesgame.R;
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private EconomyManager economy;
    private GameLogic game;
    private GridView gridView;
    private TextView tvBalance, tvMultiplier, tvWinnings;
    private Button btnCashOut, btnNewGame;
    private LinearLayout gamePanel, menuPanel;
    private int mode = 0; // 0 = single, 1 = 2p offline
    private int currentPlayer = 1; // for 2p

    private ArrayList<String> tileStates; // for adapter
    private TileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // we'll create this next

        db = new DatabaseHelper(this);
        economy = db.getEconomy();

        // Find views (define in XML)
        tvBalance = findViewById(R.id.tv_balance);
        menuPanel = findViewById(R.id.menu_panel);
        gamePanel = findViewById(R.id.game_panel);
        gridView = findViewById(R.id.grid_view);
        tvMultiplier = findViewById(R.id.tv_multiplier);
        tvWinnings = findViewById(R.id.tv_winnings);
        btnCashOut = findViewById(R.id.btn_cashout);
        btnNewGame = findViewById(R.id.btn_newgame);

        updateBalance();

        // Menu buttons - you need to add them in XML or create programmatically
        // For simplicity, we'll assume XML has buttons with ids: btn_single, btn_2p

        // Example: Single Player
        findViewById(R.id.btn_single).setOnClickListener(v -> startSinglePlayer());
        findViewById(R.id.btn_2p).setOnClickListener(v -> start2POffline());

        btnCashOut.setOnClickListener(v -> cashOut());
        btnNewGame.setOnClickListener(v -> resetToMenu());
    }

    private void updateBalance() {
        tvBalance.setText("Balance: " + economy.getBalance() + " coins");
    }

    private void startSinglePlayer() {
        mode = 0;
        showBetSetup(); // You can make a simple dialog or another layout for bet + mines input
        // For brevity: hardcode or use EditText in XML
        // Example:
        // long bet = 100; int mines = 5;
        // if (!economy.placeBet(bet)) { Toast... return; }
        // game = new GameLogic(bet, mines);
        // startGameUI();
    }

    private void start2POffline() {
        mode = 1;
        // Similar, no bet, just start with default
        game = new GameLogic(0, 5); // no real bet
        startGameUI();
    }

    private void startGameUI() {
        menuPanel.setVisibility(View.GONE);
        gamePanel.setVisibility(View.VISIBLE);

        tileStates = new ArrayList<>();
        for (int i = 0; i < 25; i++) tileStates.add("❓");

        adapter = new TileAdapter();
        gridView.setAdapter(adapter);
        gridView.setNumColumns(5);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (game == null) return;

            boolean safe = game.clickTile(position);
            updateTile(position, safe);

            if (!safe) {
                explodeMine(position);
                if (mode == 0) {
                    Toast.makeText(this, "Mine hit! You lost the bet.", Toast.LENGTH_LONG).show();
                    vibrate();
                    resetToMenu();
                } else {
                    // 2P: other player wins round
                    Toast.makeText(this, "Player " + currentPlayer + " hit mine! Player " + (3-currentPlayer) + " wins round!", Toast.LENGTH_SHORT).show();
                    currentPlayer = 3 - currentPlayer;
                    // continue or new round
                }
            } else {
                updateUIAfterClick();
            }
        });

        updateUIAfterClick();
    }

    private void updateTile(int pos, boolean safe) {
        tileStates.set(pos, safe ? "💎" : "💣");
        adapter.notifyDataSetChanged();
    }

    private void explodeMine(int pos) {
        // reveal all mines optionally
        for (int i = 0; i < 25; i++) {
            if (game.isMineTile(i)) {
                tileStates.set(i, "💣");
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateUIAfterClick() {
        tvMultiplier.setText("Multiplier: x" + String.format("%.2f", game.getMultiplier()));
        tvWinnings.setText("Winnings: " + (int)game.getCurrentWinnings());
    }

    private void cashOut() {
        if (mode == 0 && game != null) {
            long win = (long) game.getCurrentWinnings();
            economy.addWinnings(win);
            Toast.makeText(this, "Cashed out " + win + " coins!", Toast.LENGTH_SHORT).show();
            updateBalance();
        }
        resetToMenu();
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (v != null) v.vibrate(300);
    }

    private void resetToMenu() {
        gamePanel.setVisibility(View.GONE);
        menuPanel.setVisibility(View.VISIBLE);
        updateBalance();
    }

    // Simple inner Adapter for GridView
    private class TileAdapter extends ArrayAdapter<String> {
        public TileAdapter() {
            super(MainActivity.this, android.R.layout.simple_list_item_1, tileStates);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView tv = (TextView) view;
            tv.setTextSize(24);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            return tv;
        }
    }
}
