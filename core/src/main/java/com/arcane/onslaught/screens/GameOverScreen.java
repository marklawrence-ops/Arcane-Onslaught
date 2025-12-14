package com.arcane.onslaught.screens;

import com.arcane.onslaught.utils.FontManager;
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
import com.badlogic.gdx.math.Rectangle; // Import
import com.badlogic.gdx.math.Vector3;   // Import
import com.arcane.onslaught.utils.Constants;
import com.arcane.onslaught.utils.SoundManager; // Import

public class GameOverScreen implements Screen {
    private Game game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont statFont;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout layout;

    private int finalLevel;
    private float survivalTime;
    private float fadeIn = 0f;

    // --- NEW: Mouse & Sound Support ---
    private Vector3 touchPoint;
    private Rectangle retryBtnBounds;
    private Rectangle menuBtnBounds;
    private boolean retryHovered = false;
    private boolean menuHovered = false;

    public GameOverScreen(Game game, float survivalTime, int finalLevel) {
        this.game = game;
        this.survivalTime = survivalTime;
        this.finalLevel = finalLevel;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);

        // Fix Camera Position
        viewport.apply();
        camera.position.set(Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        layout = new GlyphLayout();
        touchPoint = new Vector3();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(5f);
        titleFont.setColor(Color.RED);

        statFont = new BitmapFont();
        statFont.getData().setScale(2.5f);
        statFont.setColor(Color.GOLD);

        font = new BitmapFont();
        font.getData().setScale(1.8f);
        font.setColor(Color.WHITE);

        // --- FONT SETUP ---
        FontManager.getInstance().load();
        titleFont = FontManager.getInstance().generateFont(80, Color.RED);
        statFont = FontManager.getInstance().generateFont(40, Color.GOLD);
        font = FontManager.getInstance().generateFont(32, Color.WHITE);

        // Define Button Areas for Mouse
        float centerX = Constants.SCREEN_WIDTH / 2f;
        float centerY = Constants.SCREEN_HEIGHT / 2f;
        // Approx bounds based on text position
        retryBtnBounds = new Rectangle(centerX - 100, centerY - 150, 200, 50);
        menuBtnBounds = new Rectangle(centerX - 150, centerY - 200, 300, 50);

        // --- AUDIO: Stop Music & Play Sad Theme ---
        SoundManager.getInstance().stopMusic();
        SoundManager.getInstance().play("gameover");

        centerX = Constants.SCREEN_WIDTH / 2f;
        centerY = Constants.SCREEN_HEIGHT / 2f;
        retryBtnBounds = new Rectangle(centerX - 100, centerY - 150, 200, 50);
        menuBtnBounds = new Rectangle(centerX - 150, centerY - 200, 300, 50);
    }

    @Override
    public void render(float delta) {
        // Fullscreen Toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode((int)Constants.SCREEN_WIDTH, (int)Constants.SCREEN_HEIGHT);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        fadeIn = Math.min(1f, fadeIn + delta * 2f);

        Gdx.gl.glClearColor(0.15f, 0.05f, 0.05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        // Draw Background Box
        drawStatsBox();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        titleFont.setColor(0, 0, 0, fadeIn * 0.7f);
        layout.setText(titleFont, "GAME OVER");
        titleFont.draw(batch, "GAME OVER", Constants.SCREEN_WIDTH / 2f - layout.width / 2 + 4, Constants.SCREEN_HEIGHT - 96f);

        titleFont.setColor(1f, 0.2f, 0.2f, fadeIn);
        titleFont.draw(batch, "GAME OVER", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT - 100f);

        // Stats
        statFont.setColor(Color.GOLD.r, Color.GOLD.g, Color.GOLD.b, fadeIn);
        String levelText = "Final Level: " + finalLevel;
        layout.setText(statFont, levelText);
        statFont.draw(batch, levelText, Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f + 60f);

        String timeText = "Survived: " + formatTime(survivalTime);
        layout.setText(statFont, timeText);
        statFont.draw(batch, timeText, Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f);

        // Buttons (With Hover Color Logic)
        font.getData().setScale(1.8f);

        // Retry Button
        font.setColor(retryHovered ? Color.YELLOW : new Color(0.3f, 1f, 0.3f, fadeIn));
        String retryText = "[ R ] Retry";
        layout.setText(font, retryText);
        font.draw(batch, retryText, Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f - 120f);

        // Menu Button
        font.setColor(menuHovered ? Color.YELLOW : new Color(1f, 0.5f, 0.5f, fadeIn));
        String menuText = "[ Q ] Main Menu";
        layout.setText(font, menuText);
        font.draw(batch, menuText, Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f - 170f);

        batch.end();

        if (fadeIn >= 1f) {
            handleInput();
        }
    }

    private void handleInput() {
        // 1. Mouse/Touch Logic
        camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

        // Retry Hover
        boolean nowRetry = retryBtnBounds.contains(touchPoint.x, touchPoint.y);
        if (nowRetry && !retryHovered) SoundManager.getInstance().play("ui_hover");
        retryHovered = nowRetry;

        // Menu Hover
        boolean nowMenu = menuBtnBounds.contains(touchPoint.x, touchPoint.y);
        if (nowMenu && !menuHovered) SoundManager.getInstance().play("ui_hover");
        menuHovered = nowMenu;

        // Mouse Click
        if (Gdx.input.justTouched()) {
            if (retryHovered) {
                SoundManager.getInstance().play("ui_click");
                game.setScreen(new GameScreen(game));
            } else if (menuHovered) {
                SoundManager.getInstance().play("ui_click");
                game.setScreen(new MenuScreen(game));
            }
        }

        // 2. Keyboard Logic
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
        float boxHeight = 200f;
        float x = Constants.SCREEN_WIDTH / 2f - boxWidth / 2f;
        float y = Constants.SCREEN_HEIGHT / 2f - boxHeight / 2f + 20f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.05f, 0.05f, fadeIn * 0.8f);
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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // Keep camera centered
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
        shapeRenderer.dispose();
        if (titleFont != null) titleFont.dispose();
        if (statFont != null) statFont.dispose();
        if (font != null) font.dispose();
    }
}
