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
 * Renders UI elements like health bar, XP bar, and stats
 */
public class UISystem extends EntitySystem {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;

    private ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);
    private ComponentMapper<PlayerComponent> pm = ComponentMapper.getFor(PlayerComponent.class);

    private Entity player;

    public UISystem(ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        this.shapeRenderer = shapeRenderer;
        this.camera = camera;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(1.5f);
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

        // Draw health bar
        drawHealthBar(health);

        // Draw XP bar
        drawXPBar(playerComp);

        // Draw stats text
        drawStats(health, playerComp);
    }

    private void drawHealthBar(HealthComponent health) {
        float barWidth = 300f;
        float barHeight = 25f;
        float x = 20f;
        float y = Constants.SCREEN_HEIGHT - 50f;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Background (dark red)
        shapeRenderer.setColor(0.3f, 0, 0, 1);
        shapeRenderer.rect(x, y, barWidth, barHeight);

        // Health bar (color changes based on health %)
        float healthPercent = health.currentHealth / health.maxHealth;
        Color healthColor;
        if (healthPercent > 0.5f) {
            healthColor = Color.GREEN;
        } else if (healthPercent > 0.25f) {
            healthColor = Color.YELLOW;
        } else {
            healthColor = Color.RED;
        }

        shapeRenderer.setColor(healthColor);
        shapeRenderer.rect(x, y, barWidth * healthPercent, barHeight);

        shapeRenderer.end();

        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        shapeRenderer.end();
    }

    private void drawXPBar(PlayerComponent player) {
        float barWidth = 300f;
        float barHeight = 20f;
        float x = 20f;
        float y = Constants.SCREEN_HEIGHT - 85f;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Background (dark blue)
        shapeRenderer.setColor(0, 0, 0.3f, 1);
        shapeRenderer.rect(x, y, barWidth, barHeight);

        // XP bar (cyan)
        float xpPercent = Math.min(1.0f, player.xp / player.xpToNextLevel);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(x, y, barWidth * xpPercent, barHeight);

        shapeRenderer.end();

        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        shapeRenderer.end();
    }

    private void drawStats(HealthComponent health, PlayerComponent player) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Level (top)
        font.draw(batch,
            String.format("Level: %d", player.level),
            25f, Constants.SCREEN_HEIGHT - 15f);

        // Health text
        font.draw(batch,
            String.format("HP: %.0f/%.0f", health.currentHealth, health.maxHealth),
            330f, Constants.SCREEN_HEIGHT - 30f);

        // XP text
        font.draw(batch,
            String.format("XP: %.0f/%.0f", player.xp, player.xpToNextLevel),
            330f, Constants.SCREEN_HEIGHT - 65f);

        batch.end();
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
