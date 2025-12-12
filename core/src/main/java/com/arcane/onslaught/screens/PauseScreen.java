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

public class PauseScreen implements Screen {
    private Game game;
    private GameScreen gameScreen;
    private OrthographicCamera camera;
    private Viewport viewport; // --- NEW ---
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout layout;

    private int selectedOption = 0;
    private String[] options = {"Resume", "Restart", "Main Menu"};

    public PauseScreen(Game game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        // --- NEW ---
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
        viewport.apply();
        camera.position.set(Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        layout = new GlyphLayout();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);
        titleFont.setColor(Color.CYAN);

        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);
    }

    @Override
    public void render(float delta) {
        // Toggle Fullscreen (and update layout instantly)
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode((int)Constants.SCREEN_WIDTH, (int)Constants.SCREEN_HEIGHT);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        camera.update();

        // Draw game underneath (frozen)
        gameScreen.renderPaused(delta);

        // Draw dark overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.75f);
        shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        shapeRenderer.end();

        drawMenuBox();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        titleFont.setColor(0, 0, 0, 0.7f);
        layout.setText(titleFont, "PAUSED");
        titleFont.draw(batch, "PAUSED", Constants.SCREEN_WIDTH / 2f - layout.width / 2 + 3, Constants.SCREEN_HEIGHT / 2f + 147f);

        titleFont.setColor(Color.CYAN);
        titleFont.draw(batch, "PAUSED", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f + 150f);

        float startY = Constants.SCREEN_HEIGHT / 2f + 30f;
        float spacing = 60f;

        for (int i = 0; i < options.length; i++) {
            boolean isSelected = (i == selectedOption);
            font.getData().setScale(isSelected ? 2.3f : 2f);

            String optionText;
            if (isSelected) {
                font.setColor(Color.YELLOW);
                optionText = "> " + options[i] + " <";
            } else {
                font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
                optionText = options[i];
            }

            layout.setText(font, optionText);
            font.draw(batch, optionText, Constants.SCREEN_WIDTH / 2f - layout.width / 2, startY - i * spacing);
        }

        font.getData().setScale(1.2f);
        font.setColor(new Color(0.5f, 0.5f, 0.5f, 0.8f));
        String controls = "Arrow Keys / WASD - Navigate    ENTER / ESC - Select";
        layout.setText(font, controls);
        font.draw(batch, controls, Constants.SCREEN_WIDTH / 2f - layout.width / 2, 80f);

        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        handleInput();
    }

    private void drawMenuBox() {
        float boxWidth = 500f;
        float boxHeight = 350f;
        float x = Constants.SCREEN_WIDTH / 2f - boxWidth / 2f;
        float y = Constants.SCREEN_HEIGHT / 2f - boxHeight / 2f;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.9f);
        shapeRenderer.rect(x, y, boxWidth, boxHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(x, y, boxWidth, boxHeight);
        shapeRenderer.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedOption = (selectedOption - 1 + options.length) % options.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedOption = (selectedOption + 1) % options.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            executeOption(selectedOption);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) executeOption(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) executeOption(2);
    }

    private void executeOption(int option) {
        switch (option) {
            case 0: game.setScreen(gameScreen); break;
            case 1: game.setScreen(new GameScreen(game)); break;
            case 2: game.setScreen(new MenuScreen(game)); break;
        }
    }

    @Override
    public void resize(int width, int height) {
        // --- NEW: Resize BOTH Viewports ---
        viewport.update(width, height, true);
        gameScreen.resize(width, height); // Keep background scaled correctly
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
