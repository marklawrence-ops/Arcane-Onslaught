package com.arcane.onslaught.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class FontManager {
    private static FontManager instance;
    private FreeTypeFontGenerator generator;
    private boolean isLoaded = false;

    private FontManager() {}

    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    public void load() {
        if (isLoaded) return;

        FileHandle fontFile = Gdx.files.internal("fonts/Silver.ttf");
        if (fontFile.exists()) {
            generator = new FreeTypeFontGenerator(fontFile);
            isLoaded = true;
        } else {
            System.out.println("Font file not found: fonts/Silver.ttf");
        }
    }

    public BitmapFont generateFont(int size) {
        return generateFont(size, Color.WHITE);
    }

    public BitmapFont generateFont(int size, Color color) {
        if (!isLoaded) {
            // Fallback if TTF missing
            BitmapFont font = new BitmapFont();
            font.getData().setScale(size / 16f);
            font.setColor(color);
            return font;
        }

        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.borderWidth = 2;
        parameter.borderColor = Color.BLACK;
        parameter.shadowOffsetX = 3;
        parameter.shadowOffsetY = 3;
        parameter.shadowColor = new Color(0, 0, 0, 0.5f);
        // Smoother scaling
        parameter.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
        parameter.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;

        return generator.generateFont(parameter);
    }

    public void dispose() {
        if (generator != null) {
            generator.dispose();
            generator = null;
        }
        isLoaded = false;
    }
}
