package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.utils.Constants;
import com.arcane.onslaught.utils.FontManager;

public class UISystem extends EntitySystem {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private OrthographicCamera camera;

    // Fonts (Generated at correct sizes)
    private BitmapFont barFont;      // For "100/100"
    private BitmapFont levelFont;    // For "LEVEL 5"

    private GlyphLayout layout;      // Helper to center text

    private ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);
    private ComponentMapper<PlayerComponent> pm = ComponentMapper.getFor(PlayerComponent.class);

    private Entity player;

    // UI Constants
    private final float BAR_X = 20f;
    private final float BAR_WIDTH = 300f;
    private final float HP_BAR_HEIGHT = 30f;
    private final float XP_BAR_HEIGHT = 12f;
    private final float BORDER_THICKNESS = 3f;

    // Y-Coordinates (Fixed positions)
    private final float HP_BAR_Y = Constants.SCREEN_HEIGHT - 60f;
    private final float XP_BAR_Y = Constants.SCREEN_HEIGHT - 85f;
    private final float LEVEL_TEXT_Y = Constants.SCREEN_HEIGHT - 15f;

    public UISystem(ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        this.shapeRenderer = shapeRenderer;
        this.camera = camera;
        this.batch = new SpriteBatch();
        this.layout = new GlyphLayout();

        // 1. Load Fonts properly (No scaling!)
        FontManager.getInstance().load();

        // Generate crisp fonts
        this.barFont = FontManager.getInstance().generateFont(18, Color.WHITE);
        // Use a Gold color for the Level Title, Size 32
        this.levelFont = FontManager.getInstance().generateFont(32, new Color(1f, 0.8f, 0.2f, 1f));
    }

    @Override
    public void update(float deltaTime) {
        player = null;
        Family playerFamily = Family.all(PlayerComponent.class).get();
        for (Entity entity : getEngine().getEntitiesFor(playerFamily)) {
            player = entity;
            break;
        }

        if (player == null) return;

        HealthComponent health = hm.get(player);
        PlayerComponent playerComp = pm.get(player);

        if (health == null || playerComp == null) return;

        // --- PHASE 1: DRAW SHAPES (Bars) ---
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawHealthBar(health);
        drawXPBar(playerComp);

        shapeRenderer.end();

        // --- PHASE 2: DRAW TEXT ---
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        drawUIStats(health, playerComp);

        batch.end();
    }

    private void drawHealthBar(HealthComponent health) {
        float percent = Math.max(0, health.currentHealth / health.maxHealth);

        // 1. Border (Black)
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(BAR_X - BORDER_THICKNESS, HP_BAR_Y - BORDER_THICKNESS,
            BAR_WIDTH + BORDER_THICKNESS*2, HP_BAR_HEIGHT + BORDER_THICKNESS*2);

        // 2. Background Tray (Dark Grey)
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(BAR_X, HP_BAR_Y, BAR_WIDTH, HP_BAR_HEIGHT);

        // 3. Health Fill (Gradient)
        // Determine color based on health status
        Color startColor, endColor;
        if (percent > 0.5f) {
            startColor = new Color(0f, 0.6f, 0f, 1f); // Dark Green
            endColor = new Color(0.2f, 1f, 0.2f, 1f); // Bright Green
        } else if (percent > 0.25f) {
            startColor = new Color(0.8f, 0.6f, 0f, 1f); // Dark Yellow
            endColor = new Color(1f, 0.9f, 0.2f, 1f); // Bright Yellow
        } else {
            startColor = new Color(0.8f, 0f, 0f, 1f); // Dark Red
            endColor = new Color(1f, 0.2f, 0.2f, 1f); // Bright Red
        }

        // Draw horizontal gradient rect
        if (percent > 0) {
            shapeRenderer.rect(
                BAR_X, HP_BAR_Y,
                BAR_WIDTH * percent, HP_BAR_HEIGHT,
                startColor, endColor, endColor, startColor // Gradient colors
            );
        }

        // 4. Glass/Gloss Effect (Top Half)
        shapeRenderer.setColor(1f, 1f, 1f, 0.15f); // Transparent White
        shapeRenderer.rect(BAR_X, HP_BAR_Y + HP_BAR_HEIGHT/2, BAR_WIDTH * percent, HP_BAR_HEIGHT/2);
    }

    private void drawXPBar(PlayerComponent player) {
        float percent = Math.min(1.0f, player.xp / player.xpToNextLevel);

        // 1. Border
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(BAR_X - BORDER_THICKNESS, XP_BAR_Y - BORDER_THICKNESS,
            BAR_WIDTH + BORDER_THICKNESS*2, XP_BAR_HEIGHT + BORDER_THICKNESS*2);

        // 2. Background
        shapeRenderer.setColor(0.1f, 0.1f, 0.2f, 1f);
        shapeRenderer.rect(BAR_X, XP_BAR_Y, BAR_WIDTH, XP_BAR_HEIGHT);

        // 3. Fill (Cyan Gradient)
        if (percent > 0) {
            Color c1 = new Color(0f, 0.4f, 0.8f, 1f); // Dark Blue
            Color c2 = new Color(0f, 0.8f, 1f, 1f);   // Cyan
            shapeRenderer.rect(
                BAR_X, XP_BAR_Y,
                BAR_WIDTH * percent, XP_BAR_HEIGHT,
                c1, c2, c2, c1
            );
        }
    }

    private void drawUIStats(HealthComponent health, PlayerComponent player) {
        // 1. Level Text (Top Left)
        String lvlStr = "LEVEL " + player.level;
        levelFont.setColor(1f, 0.85f, 0.2f, 1f); // Gold
        levelFont.draw(batch, lvlStr, BAR_X, LEVEL_TEXT_Y);

        // 2. Health Text (Centered in Bar)
        String hpStr = (int)health.currentHealth + " / " + (int)health.maxHealth;
        layout.setText(barFont, hpStr);

        float hpTextX = BAR_X + (BAR_WIDTH - layout.width) / 2;
        float hpTextY = HP_BAR_Y + (HP_BAR_HEIGHT + layout.height) / 2 - 2; // -2 visual adjust

        // Drop Shadow for text readability
        barFont.setColor(0f, 0f, 0f, 0.5f);
        barFont.draw(batch, hpStr, hpTextX + 2, hpTextY - 2);

        // Main Text
        barFont.setColor(Color.WHITE);
        barFont.draw(batch, hpStr, hpTextX, hpTextY);

        // 3. XP Text (Optional, small overlay or below)
        // Since XP bar is thin, let's draw text just to the right of it or centered
        // For thin bars, usually no text inside looks cleaner,
        // but if you want it:
        /*
        String xpStr = (int)player.xp + "";
        barFont.getData().setScale(0.8f); // Slightly smaller
        barFont.draw(batch, xpStr, BAR_X + BAR_WIDTH + 10, XP_BAR_Y + 10);
        barFont.getData().setScale(1.0f); // Reset
        */
    }

    public void dispose() {
        batch.dispose();
        if (barFont != null) barFont.dispose();
        if (levelFont != null) levelFont.dispose();
    }
}
