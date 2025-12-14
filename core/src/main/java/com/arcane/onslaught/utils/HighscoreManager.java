package com.arcane.onslaught.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class HighscoreManager {
    private static final String PREF_NAME = "onslaught_save_data";
    private static final String KEY_LEVEL = "best_level";
    private static final String KEY_TIME = "best_time";

    private static Preferences getPrefs() {
        return Gdx.app.getPreferences(PREF_NAME);
    }

    public static int getBestLevel() {
        return getPrefs().getInteger(KEY_LEVEL, 1);
    }

    public static float getBestTime() {
        return getPrefs().getFloat(KEY_TIME, 0f);
    }

    /**
     * Checks if current stats are highscores and saves them if necessary.
     * @return true if EITHER level or time was a new record.
     */
    public static boolean checkAndSave(int currentLevel, float currentTime) {
        Preferences prefs = getPrefs();
        boolean isNewRecord = false;

        // Check Level Record
        if (currentLevel > getBestLevel()) {
            prefs.putInteger(KEY_LEVEL, currentLevel);
            isNewRecord = true;
        }

        // Check Time Record
        if (currentTime > getBestTime()) {
            prefs.putFloat(KEY_TIME, currentTime);
            isNewRecord = true;
        }

        if (isNewRecord) {
            prefs.flush(); // IMPORTANT: Persist data to disk
        }
        return isNewRecord;
    }

    public static String formatTime(float timeSeconds) {
        int mins = (int)(timeSeconds / 60);
        int secs = (int)(timeSeconds % 60);
        return String.format("%02d:%02d", mins, secs);
    }
}
