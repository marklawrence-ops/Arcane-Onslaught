package com.arcane.onslaught.entities.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.arcane.onslaught.entities.components.*;

public class DamageIndicatorSystem extends IteratingSystem {
    private ComponentMapper<PositionComponent> posMapper = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<DamageIndicatorComponent> damageMapper = ComponentMapper.getFor(DamageIndicatorComponent.class);
    private ComponentMapper<VelocityComponent> velMapper = ComponentMapper.getFor(VelocityComponent.class);

    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont critFont;
    private OrthographicCamera camera;
    private GlyphLayout layout;

    public DamageIndicatorSystem(SpriteBatch batch, OrthographicCamera camera) {
        super(Family.all(DamageIndicatorComponent.class, PositionComponent.class).get());
        this.batch = batch;
        this.camera = camera;
        this.layout = new GlyphLayout();

        font = new BitmapFont();
        font.getData().setScale(1.5f);
        font.setUseIntegerPositions(false);

        critFont = new BitmapFont();
        critFont.getData().setScale(2.2f);
        critFont.setUseIntegerPositions(false);
    }

    @Override
    public void update(float deltaTime) {
        // Run logic first
        super.update(deltaTime);

        // Prepare for rendering
        batch.setColor(Color.WHITE);
        batch.setProjectionMatrix(camera.combined);

        // Only draw if there are entities
        if (getEntities().size() > 0) {
            batch.begin();
            try {
                for (Entity entity : getEntities()) {
                    renderEntity(entity);
                }
            } catch (Exception e) {
                System.err.println("Render Error: " + e.getMessage());
            } finally {
                // Ensure batch always closes to prevent black screen
                if (batch.isDrawing()) {
                    batch.end();
                }
            }
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent pos = posMapper.get(entity);
        DamageIndicatorComponent dmg = damageMapper.get(entity);
        VelocityComponent vel = velMapper.get(entity);

        // Update time
        dmg.timeAlive += deltaTime;

        // Move upward
        if (vel != null) {
            pos.position.y += vel.velocity.y * deltaTime;
        }

        // --- MATH SAFETY FIX ---
        // 1. Prevent Division by Zero: Ensure lifetime is never 0
        if (dmg.lifetime <= 0.001f) {
            dmg.lifetime = 1.0f; // Default to 1 second if corrupted
        }

        // 2. Calculate Alpha safely
        float alpha = 1.0f - (dmg.timeAlive / dmg.lifetime);

        // 3. Clamp Alpha to valid range [0, 1]
        // This removes NaN/Infinity values that crash the GPU
        if (Float.isNaN(alpha) || Float.isInfinite(alpha)) {
            alpha = 1.0f;
        }
        alpha = Math.max(0f, Math.min(1f, alpha));

        dmg.color.a = alpha;

        // Remove if expired
        if (dmg.timeAlive >= dmg.lifetime) {
            getEngine().removeEntity(entity);
        }
    }

    private void renderEntity(Entity entity) {
        PositionComponent pos = posMapper.get(entity);
        DamageIndicatorComponent dmg = damageMapper.get(entity);

        if (pos == null || dmg == null) return;

        // Don't draw invisible text
        if (dmg.color.a <= 0.01f) return;

        // --- COORDINATE SAFETY FIX ---
        // Prevent drawing at invalid positions (NaN/Infinity)
        if (Float.isNaN(pos.position.x) || Float.isNaN(pos.position.y)) return;

        BitmapFont fontToUse = dmg.isCritical ? critFont : font;
        fontToUse.setColor(dmg.color);

        String text = String.valueOf((int)dmg.damage);
        if (dmg.isCritical) {
            text = "CRIT! " + text;
        }

        layout.setText(fontToUse, text);
        float textX = pos.position.x - layout.width / 2;
        float textY = pos.position.y + layout.height / 2;

        // Final check before OpenGL call
        if (!Float.isNaN(textX) && !Float.isNaN(textY)) {
            fontToUse.draw(batch, text, textX, textY);
        }
    }

    public void dispose() {
        font.dispose();
        critFont.dispose();
    }
}
