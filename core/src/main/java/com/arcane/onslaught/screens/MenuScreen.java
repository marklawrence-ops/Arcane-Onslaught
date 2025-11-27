package com.arcane.onslaught.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.arcane.onslaught.utils.Constants;

/**
 * Main menu screen - first screen when game starts
 */
public class MenuScreen implements Screen {
    private Game game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    private float time = 0;

    public MenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        camera.setToOrtho(false);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);
        titleFont.setColor(Color.CYAN);

        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);

        System.out.println("Main Menu - Press ENTER to start!");
    }

    @Override
    public void render(float delta) {
        time += delta;

        // Clear screen
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw animated background
        drawBackground();

        // Draw UI
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        String title = "ARCANE ONSLAUGHT";
        float titleWidth = titleFont.getRegion().getRegionWidth() * 4f * title.length() * 0.6f;
        titleFont.draw(batch, title,
            Constants.SCREEN_WIDTH / 2f - titleWidth / 2f,
            Constants.SCREEN_HEIGHT - 150f);

        // Instructions (blinking)
        font.getData().setScale(2f);
        font.setColor(Color.GREEN);
        if (((int)(time * 2) % 2) == 0) {
            String start = "PRESS ENTER TO START";
            // Calculate proper width for centered text
            float charWidth = 12f; // Approximate character width at scale 2
            float startWidth = start.length() * charWidth;
            font.draw(batch, start,
                Constants.SCREEN_WIDTH / 2f - startWidth / 2f,
                Constants.SCREEN_HEIGHT / 2f);
        }

        // Controls
        font.getData().setScale(1.5f);
        font.setColor(Color.GRAY);
        font.draw(batch, "WASD - Move", 100f, 150f);
        font.draw(batch, "ESC - Pause", 100f, 100f);
        font.draw(batch, "Auto-cast spells", 100f, 50f);

        batch.end();

        // Check for input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game));
        }
    }

    private void drawBackground() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Animated stars/particles
        for (int i = 0; i < 50; i++) {
            float x = (i * 123.456f) % Constants.SCREEN_WIDTH;
            float y = ((i * 234.567f + time * 50f) % Constants.SCREEN_HEIGHT);
            float size = 2f + (i % 3);

            shapeRenderer.setColor(0.5f, 0.5f, 1f, 0.5f);
            shapeRenderer.circle(x, y, size);
        }

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
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
        font.dispose();
        shapeRenderer.dispose();
    }
}
