package com.arcane.onslaught.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport; // Import
import com.badlogic.gdx.utils.viewport.Viewport;     // Import
import com.arcane.onslaught.utils.Constants;

/**
 * Main menu with animated background
 */
public class MenuScreen implements Screen {
    private Game game;
    private OrthographicCamera camera;
    private Viewport viewport; // --- NEW: Viewport ---
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont subtitleFont;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout layout;

    private float time = 0;
    private Particle[] particles;

    // Simple particle class for background
    private static class Particle {
        float x, y, vx, vy, size;
        Color color;

        Particle() {
            reset();
        }

        void reset() {
            x = (float)(Math.random() * Constants.SCREEN_WIDTH);
            y = (float)(Math.random() * Constants.SCREEN_HEIGHT);
            vx = (float)(Math.random() * 20 - 10);
            vy = (float)(Math.random() * 20 - 10);
            size = (float)(Math.random() * 3 + 1);

            float r = (float)Math.random();
            if (r < 0.33f) {
                color = new Color(0.3f, 0.6f, 1f, 0.7f);
            } else if (r < 0.66f) {
                color = new Color(0.8f, 0.3f, 1f, 0.7f);
            } else {
                color = new Color(0.3f, 1f, 1f, 0.7f);
            }
        }

        void update(float delta) {
            x += vx * delta;
            y += vy * delta;
            if (x < 0 || x > Constants.SCREEN_WIDTH ||
                y < 0 || y > Constants.SCREEN_HEIGHT) {
                reset();
            }
        }
    }

    public MenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        // --- NEW: Init Viewport ---
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
        viewport.apply();
        camera.position.set(Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        layout = new GlyphLayout();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(5f);
        titleFont.setColor(new Color(0.3f, 0.8f, 1f, 1f));

        subtitleFont = new BitmapFont();
        subtitleFont.getData().setScale(2.5f);
        subtitleFont.setColor(new Color(1f, 0.5f, 1f, 1f));

        font = new BitmapFont();
        font.getData().setScale(1.8f);
        font.setColor(Color.WHITE);

        particles = new Particle[100];
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle();
        }
    }

    @Override
    public void render(float delta) {
        time += delta;

        // Toggle Fullscreen on Menu too
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode((int)Constants.SCREEN_WIDTH, (int)Constants.SCREEN_HEIGHT);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera for rendering
        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Particle p : particles) {
            p.update(delta);
            shapeRenderer.setColor(p.color);
            shapeRenderer.circle(p.x, p.y, p.size);
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        titleFont.setColor(0, 0, 0, 0.5f);
        layout.setText(titleFont, "ARCANE ONSLAUGHT");
        titleFont.draw(batch, "ARCANE ONSLAUGHT", Constants.SCREEN_WIDTH / 2f - layout.width / 2 + 3, Constants.SCREEN_HEIGHT - 147f);

        titleFont.setColor(new Color(0.3f, 0.8f, 1f, 1f));
        titleFont.draw(batch, "ARCANE ONSLAUGHT", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT - 150f);

        subtitleFont.setColor(new Color(1f, 0.5f, 1f, 0.8f));
        layout.setText(subtitleFont, "Survive the Endless Horde");
        subtitleFont.draw(batch, "Survive the Endless Horde", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT - 250f);

        float pulse = 0.6f + 0.4f * (float)Math.sin(time * 3);
        font.getData().setScale(2.2f);
        font.setColor(0.3f, 1f, 0.3f, pulse);
        layout.setText(font, "PRESS ENTER TO START");
        font.draw(batch, "PRESS ENTER TO START", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f);

        font.getData().setScale(1.5f);
        font.setColor(new Color(0.7f, 0.7f, 0.7f, 0.8f));
        font.draw(batch, "Controls:", 100f, 250f);
        font.draw(batch, "WASD - Move", 100f, 200f);
        font.draw(batch, "ESC - Pause", 100f, 150f);
        font.draw(batch, "Spells cast automatically", 100f, 100f);

        font.getData().setScale(1f);
        font.setColor(new Color(0.5f, 0.5f, 0.5f, 0.6f));
        font.draw(batch, "v1.0 - Roguelike Survivor", 20f, 30f);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        // --- NEW: Update Viewport ---
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        subtitleFont.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}
