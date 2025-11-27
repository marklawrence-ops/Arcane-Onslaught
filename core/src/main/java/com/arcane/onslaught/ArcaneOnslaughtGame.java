package com.arcane.onslaught;

import com.badlogic.gdx.Game;
import com.arcane.onslaught.screens.MenuScreen;

/**
 * Main game class - entry point
 */
public class ArcaneOnslaughtGame extends Game {

    @Override
    public void create() {
        // Start with menu screen
        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (screen != null) {
            screen.dispose();
        }
    }
}
