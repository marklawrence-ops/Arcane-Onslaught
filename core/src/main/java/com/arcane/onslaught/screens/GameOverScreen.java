package com.arcane.onslaught.screens;

import com.arcane.onslaught.utils.FontManager;
import com.arcane.onslaught.utils.HighscoreManager;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.arcane.onslaught.utils.Constants;
import com.arcane.onslaught.utils.SoundManager;

public class GameOverScreen implements Screen {
    private Game game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont statFont;
    private BitmapFont font;
    private BitmapFont scoreFont;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout layout;

    private int finalLevel;
    private float survivalTime;
    private float fadeIn = 0f;
    private boolean isNewRecord;

    private Vector3 touchPoint;
    private Rectangle retryBtnBounds;
    private Rectangle menuBtnBounds;
    private boolean retryHovered = false;
    private boolean menuHovered = false;

    public GameOverScreen(Game game, float survivalTime, int finalLevel, boolean isNewRecord) {
        this.game = game;
        this.survivalTime = survivalTime;
        this.finalLevel = finalLevel;
        this.isNewRecord = isNewRecord;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
        viewport.apply();
        camera.position.set(Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        layout = new GlyphLayout();
        touchPoint = new Vector3();

        FontManager.getInstance().load();
        titleFont = FontManager.getInstance().generateFont(80, Color.RED);
        statFont = FontManager.getInstance().generateFont(40, Color.GOLD);
        font = FontManager.getInstance().generateFont(32, Color.WHITE);
        scoreFont = FontManager.getInstance().generateFont(32, Color.YELLOW);

        float centerX = Constants.SCREEN_WIDTH / 2f;
        float centerY = Constants.SCREEN_HEIGHT / 2f;

        // Adjusted hitbox positions
        retryBtnBounds = new Rectangle(centerX - 100, centerY - 160, 200, 50);
        menuBtnBounds = new Rectangle(centerX - 150, centerY - 220, 300, 50);

        SoundManager.getInstance().stopMusic();
        SoundManager.getInstance().play("gameover");
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) Gdx.graphics.setWindowedMode((int)Constants.SCREEN_WIDTH, (int)Constants.SCREEN_HEIGHT);
            else Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }

        fadeIn = Math.min(1f, fadeIn + delta * 2f);

        Gdx.gl.glClearColor(0.15f, 0.05f, 0.05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        drawStatsBox();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 1. Title (with shadow effect)
        titleFont.setColor(0, 0, 0, fadeIn * 0.7f);
        layout.setText(titleFont, "GAME OVER");
        titleFont.draw(batch, "GAME OVER", Constants.SCREEN_WIDTH / 2f - layout.width / 2 + 4, Constants.SCREEN_HEIGHT - 96f);

        titleFont.setColor(1f, 0.2f, 0.2f, fadeIn);
        titleFont.draw(batch, "GAME OVER", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT - 100f);

        // 2. Stats
        statFont.setColor(Color.GOLD.r, Color.GOLD.g, Color.GOLD.b, fadeIn);

        // Increased Spacing: +80 instead of +60
        String levelText = "Final Level: " + finalLevel;
        layout.setText(statFont, levelText);
        statFont.draw(batch, levelText, Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f + 80f);

        // Increased Spacing: +10 instead of 0
        String timeText = "Survived: " + formatTime(survivalTime);
        layout.setText(statFont, timeText);
        statFont.draw(batch, timeText, Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f + 10f);

        // 3. Highscore Display
        if (isNewRecord) {
            scoreFont.setColor(1f, 1f, 0f, (float)Math.abs(Math.sin(System.currentTimeMillis() / 200.0))); // Flash
            String recText = "NEW RECORD!";
            layout.setText(scoreFont, recText);
            // Lowered position: -60 instead of -50
            scoreFont.draw(batch, recText, Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f - 60f);
        } else {
            scoreFont.setColor(0.8f, 0.8f, 0.8f, fadeIn);
            String bestText = "Best: Lv " + HighscoreManager.getBestLevel() + " - " + HighscoreManager.formatTime(HighscoreManager.getBestTime());
            layout.setText(scoreFont, bestText);
            scoreFont.draw(batch, bestText, Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f - 60f);
        }

        // 4. Buttons (Use White/Gray instead of Green/Red for better contrast)
        font.setColor(retryHovered ? Color.YELLOW : new Color(0.9f, 0.9f, 0.9f, fadeIn));
        layout.setText(font, "[ R ] Retry");
        font.draw(batch, "[ R ] Retry", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f - 140f);

        font.setColor(menuHovered ? Color.YELLOW : new Color(0.9f, 0.9f, 0.9f, fadeIn));
        layout.setText(font, "[ Q ] Main Menu");
        font.draw(batch, "[ Q ] Main Menu", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f - 200f);

        batch.end();

        if (fadeIn >= 1f) handleInput();
    }

    private void handleInput() {
        camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

        boolean nowRetry = retryBtnBounds.contains(touchPoint.x, touchPoint.y);
        if (nowRetry && !retryHovered) SoundManager.getInstance().play("ui_hover");
        retryHovered = nowRetry;

        boolean nowMenu = menuBtnBounds.contains(touchPoint.x, touchPoint.y);
        if (nowMenu && !menuHovered) SoundManager.getInstance().play("ui_hover");
        menuHovered = nowMenu;

        if (Gdx.input.justTouched()) {
            if (retryHovered) {
                SoundManager.getInstance().play("ui_click");
                game.setScreen(new GameScreen(game));
            } else if (menuHovered) {
                SoundManager.getInstance().play("ui_click");
                game.setScreen(new MenuScreen(game));
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            SoundManager.getInstance().play("ui_click");
            game.setScreen(new GameScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            SoundManager.getInstance().play("ui_click");
            game.setScreen(new MenuScreen(game));
        }
    }

    private void drawStatsBox() {
        float boxWidth = 500f;
        float boxHeight = 280f; // Made taller to fit spacing
        float x = Constants.SCREEN_WIDTH / 2f - boxWidth / 2f;
        float y = Constants.SCREEN_HEIGHT / 2f - boxHeight / 2f + 10f; // Adjusted Y center

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Made opacity 0.9f (Darker) for better readability
        shapeRenderer.setColor(0.1f, 0.05f, 0.05f, fadeIn * 0.9f);
        shapeRenderer.rect(x, y, boxWidth, boxHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3);
        shapeRenderer.setColor(0.8f, 0.2f, 0.2f, fadeIn);
        shapeRenderer.rect(x, y, boxWidth, boxHeight);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private String formatTime(float seconds) {
        int mins = (int)(seconds / 60);
        int secs = (int)(seconds % 60);
        return String.format("%d:%02d", mins, secs);
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); shapeRenderer.dispose(); if (titleFont != null) titleFont.dispose(); if (statFont != null) statFont.dispose(); if (font != null) font.dispose(); if (scoreFont != null) scoreFont.dispose(); }
}
