package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.utils.Constants;

/**
 * Renders modern UI with health bar, XP bar, level, and spell icons
 * FIXED: Adjusted Y-coordinates to prevent overlapping text and bars.
 */
public class UISystem extends EntitySystem {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont largeFont;
    private OrthographicCamera camera;

    private ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);
    private ComponentMapper<PlayerComponent> pm = ComponentMapper.getFor(PlayerComponent.class);

    private Entity player;

    public UISystem(ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        this.shapeRenderer = shapeRenderer;
        this.camera = camera;
        this.batch = new SpriteBatch();

        // Font for stats (HP/XP numbers)
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(1.5f);

        // Large font for "Level X" title
        this.largeFont = new BitmapFont();
        this.largeFont.setColor(Color.GOLD);
        this.largeFont.getData().setScale(2.5f);
    }

    @Override
    public void update(float deltaTime) {
        // Find player
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

        // --- PHASE 1: DRAW ALL SHAPES ---
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawHealthBarShapes(health);
        drawXPBarShapes(playerComp);

        shapeRenderer.end();

        // --- PHASE 2: DRAW ALL TEXT ---
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        drawHealthBarText(health);
        drawXPBarText(playerComp);
        drawLevelText(playerComp);

        batch.end();
    }

    private void drawHealthBarShapes(HealthComponent health) {
        float barWidth = 300f;
        float barHeight = 25f;
        float x = 20f;
        // CHANGED: Moved down to -80 (was -50) to make room for Level text
        float y = Constants.SCREEN_HEIGHT - 80f;

        float healthPercent = Math.max(0, health.currentHealth / health.maxHealth);

        // Outer border (dark)
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        shapeRenderer.rect(x - 2, y - 2, barWidth + 4, barHeight + 4);

        // Background (very dark red)
        shapeRenderer.setColor(0.15f, 0.05f, 0.05f, 0.9f);
        shapeRenderer.rect(x, y, barWidth, barHeight);

        // Health bar with color gradient based on %
        Color healthColor;
        if (healthPercent > 0.6f) {
            healthColor = new Color(0.2f, 0.9f, 0.2f, 1f); // Bright green
        } else if (healthPercent > 0.3f) {
            healthColor = new Color(0.9f, 0.8f, 0.2f, 1f); // Yellow
        } else {
            healthColor = new Color(0.9f, 0.2f, 0.2f, 1f); // Bright red
        }

        shapeRenderer.setColor(healthColor);
        shapeRenderer.rect(x, y, barWidth * healthPercent, barHeight);

        // Shine effect on top
        shapeRenderer.setColor(1f, 1f, 1f, 0.2f);
        shapeRenderer.rect(x, y + barHeight * 0.6f, barWidth * healthPercent, barHeight * 0.3f);
    }

    private void drawXPBarShapes(PlayerComponent player) {
        float barWidth = 300f;
        float barHeight = 18f;
        float x = 20f;
        // CHANGED: Moved down to -110 (was -80) to stay below Health bar
        float y = Constants.SCREEN_HEIGHT - 110f;

        float xpPercent = Math.min(1.0f, player.xp / player.xpToNextLevel);

        // Outer border
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        shapeRenderer.rect(x - 2, y - 2, barWidth + 4, barHeight + 4);

        // Background (dark blue)
        shapeRenderer.setColor(0.05f, 0.05f, 0.2f, 0.9f);
        shapeRenderer.rect(x, y, barWidth, barHeight);

        // XP bar (bright cyan with glow)
        shapeRenderer.setColor(0.2f, 0.8f, 1f, 1f);
        shapeRenderer.rect(x, y, barWidth * xpPercent, barHeight);

        // Glow on top
        shapeRenderer.setColor(0.5f, 1f, 1f, 0.3f);
        shapeRenderer.rect(x, y + barHeight * 0.5f, barWidth * xpPercent, barHeight * 0.4f);
    }

    private void drawHealthBarText(HealthComponent health) {
        float x = 20f;
        // CHANGED: Match shape Y position (-80)
        float y = Constants.SCREEN_HEIGHT - 80f;

        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
        String hpText = String.format("HP: %.0f/%.0f", health.currentHealth, health.maxHealth);
        // Offset text slightly to center it in the bar
        font.draw(batch, hpText, x + 10f, y + 18f);
    }

    private void drawXPBarText(PlayerComponent player) {
        float x = 20f;
        // CHANGED: Match shape Y position (-110)
        float y = Constants.SCREEN_HEIGHT - 110f;

        font.getData().setScale(0.9f);
        font.setColor(Color.WHITE);
        String xpText = String.format("XP: %.0f/%.0f", player.xp, player.xpToNextLevel);
        font.draw(batch, xpText, x + 10f, y + 13f);
    }

    private void drawLevelText(PlayerComponent player) {
        largeFont.setColor(Color.GOLD);
        largeFont.getData().setScale(2.5f);
        String levelText = "Level " + player.level;

        // CHANGED: Moved slightly down to -15 to allow margin from top of screen
        largeFont.draw(batch, levelText, 25f, Constants.SCREEN_HEIGHT - 15f);
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
        largeFont.dispose();
    }
}
