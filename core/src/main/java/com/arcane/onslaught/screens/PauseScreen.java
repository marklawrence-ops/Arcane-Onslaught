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

public class PauseScreen implements Screen {
    private Game game;
    private GameScreen gameScreen;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    public PauseScreen(Game game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        camera.setToOrtho(false);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        font = new BitmapFont();
        font.getData().setScale(3f);
        font.setColor(Color.WHITE);
    }

    @Override
    public void render(float delta) {
        // Draw game screen underneath (frozen)
        gameScreen.renderPaused(delta);

        // Draw dark overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Draw pause UI
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        String paused = "PAUSED";
        float pausedWidth = font.getRegion().getRegionWidth() * 3f * paused.length() * 0.6f;
        font.draw(batch, paused,
            Constants.SCREEN_WIDTH / 2f - pausedWidth / 2f,
            Constants.SCREEN_HEIGHT / 2f + 100f);

        font.getData().setScale(1.5f);
        font.draw(batch, "ESC - Resume", Constants.SCREEN_WIDTH / 2f - 100f, Constants.SCREEN_HEIGHT / 2f);
        font.draw(batch, "R - Restart", Constants.SCREEN_WIDTH / 2f - 100f, Constants.SCREEN_HEIGHT / 2f - 50f);
        font.draw(batch, "Q - Quit to Menu", Constants.SCREEN_WIDTH / 2f - 100f, Constants.SCREEN_HEIGHT / 2f - 100f);

        batch.end();

        // Handle input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(gameScreen);
        }
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
        font.dispose();
        shapeRenderer.dispose();
    }
}
