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

public class GameOverScreen implements Screen {
    private Game game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    private int finalLevel;
    private float survivalTime;

    public GameOverScreen(Game game, float survivalTime, int finalLevel) {
        this.game = game;
        this.survivalTime = survivalTime;
        this.finalLevel = finalLevel;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        camera.setToOrtho(false);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);
        titleFont.setColor(Color.RED);

        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);
    }

    @Override
    public void render(float delta) {
        // Dark red background
        Gdx.gl.glClearColor(0.2f, 0.05f, 0.05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        String title = "GAME OVER";
        float titleWidth = titleFont.getRegion().getRegionWidth() * 4f * title.length() * 0.6f;
        titleFont.draw(batch, title,
            Constants.SCREEN_WIDTH / 2f - titleWidth / 2f,
            Constants.SCREEN_HEIGHT - 150f);

        // Stats
        font.getData().setScale(2f);
        font.setColor(Color.YELLOW);
        font.draw(batch, "Final Level: " + finalLevel,
            Constants.SCREEN_WIDTH / 2f - 150f,
            Constants.SCREEN_HEIGHT / 2f + 50f);

        font.draw(batch, "Survived: " + (int)survivalTime + "s",
            Constants.SCREEN_WIDTH / 2f - 150f,
            Constants.SCREEN_HEIGHT / 2f);

        // Options
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        font.draw(batch, "R - Retry",
            Constants.SCREEN_WIDTH / 2f - 100f,
            Constants.SCREEN_HEIGHT / 2f - 100f);

        font.draw(batch, "Q - Main Menu",
            Constants.SCREEN_WIDTH / 2f - 100f,
            Constants.SCREEN_HEIGHT / 2f - 150f);

        batch.end();

        // Handle input
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            game.setScreen(new GameScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            game.setScreen(new MenuScreen(game));
        }
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
