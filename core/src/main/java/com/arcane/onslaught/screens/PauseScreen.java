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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.arcane.onslaught.utils.Constants;
import com.arcane.onslaught.utils.SoundManager;
import com.arcane.onslaught.utils.FontManager;

public class PauseScreen implements Screen {
    private Game game;
    private GameScreen gameScreen;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private GlyphLayout layout;

    private int selectedOption = 0;
    private int lastSelectedOption = -1;
    // --- UPDATED OPTIONS LIST ---
    private String[] options = {"Resume", "Restart", "Settings", "Main Menu"};

    private Vector3 touchPoint;
    private Rectangle[] menuBounds;

    public PauseScreen(Game game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
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
        titleFont = FontManager.getInstance().generateFont(80, Color.CYAN);
        // Slightly smaller font so 4 options look balanced
        font = FontManager.getInstance().generateFont(36, Color.WHITE);

        menuBounds = new Rectangle[options.length];
        for(int i=0; i<options.length; i++) menuBounds[i] = new Rectangle();

        SoundManager.getInstance().play("ui_click");
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode((int)Constants.SCREEN_WIDTH, (int)Constants.SCREEN_HEIGHT);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        camera.update();
        gameScreen.renderPaused(delta);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Darken Background
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.75f);
        shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        shapeRenderer.end();

        drawMenuBox();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        titleFont.setColor(0, 0, 0, 0.7f);
        layout.setText(titleFont, "PAUSED");
        titleFont.draw(batch, "PAUSED", Constants.SCREEN_WIDTH / 2f - layout.width / 2 + 3, Constants.SCREEN_HEIGHT / 2f + 167f);
        titleFont.setColor(Color.CYAN);
        titleFont.draw(batch, "PAUSED", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f + 170f);

        // --- UPDATED SPACING LOGIC ---
        float startY = Constants.SCREEN_HEIGHT / 2f + 50f;
        float spacing = 55f; // Tighter spacing to fit 4 items

        camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

        for (int i = 0; i < options.length; i++) {
            boolean isSelected = (i == selectedOption);

            // Highlight color
            font.setColor(isSelected ? Color.YELLOW : new Color(0.7f, 0.7f, 0.7f, 1f));
            String optionText = isSelected ? "> " + options[i] + " <" : options[i];

            layout.setText(font, optionText);
            float textWidth = layout.width;
            float textHeight = layout.height;
            float textX = Constants.SCREEN_WIDTH / 2f - textWidth / 2f;
            float textY = startY - i * spacing;

            // Update Bounds
            menuBounds[i].set(textX - 20, textY - textHeight - 10, textWidth + 40, textHeight + 20);

            // Mouse Interaction
            if (menuBounds[i].contains(touchPoint.x, touchPoint.y)) {
                if (selectedOption != i) {
                    selectedOption = i;
                    if (Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0) {
                        SoundManager.getInstance().play("ui_hover");
                    }
                }
                if (Gdx.input.justTouched()) executeOption(i);
            }

            // Draw Text
            font.draw(batch, optionText, textX, textY);
        }

        // Helper Text
        font.setColor(new Color(0.5f, 0.5f, 0.5f, 0.8f));
        // Using smaller scaling manually for this line isn't ideal with FontManager,
        // but since we generated size 24, this will look okay.
        String controls = "Arrow Keys / WASD - Navigate    ENTER / ESC - Select";
        layout.setText(font, controls);
        font.draw(batch, controls, Constants.SCREEN_WIDTH / 2f - layout.width / 2, 80f);

        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        handleInput();

        if (selectedOption != lastSelectedOption) {
            SoundManager.getInstance().play("ui_hover");
            lastSelectedOption = selectedOption;
        }
    }

    private void drawMenuBox() {
        float boxWidth = 500f;
        // --- INCREASED HEIGHT ---
        float boxHeight = 420f; // Made taller to fit 4 options comfortably
        float x = Constants.SCREEN_WIDTH / 2f - boxWidth / 2f;
        float y = Constants.SCREEN_HEIGHT / 2f - boxHeight / 2f;

        shapeRenderer.setProjectionMatrix(camera.combined);

        // Background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.9f);
        shapeRenderer.rect(x, y, boxWidth, boxHeight);
        shapeRenderer.end();

        // Border
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            executeOption(selectedOption);
        }
        // ESC acts as "Back" (Resume) if not already resuming,
        // but here we treat it as selecting the current option OR just resuming.
        // Standard behavior: ESC usually unpauses.
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            executeOption(0); // Resume
        }
    }

    private void executeOption(int option) {
        SoundManager.getInstance().play("ui_click");
        switch (option) {
            case 0: // Resume
                game.setScreen(gameScreen);
                break;
            case 1: // Restart
                game.setScreen(new GameScreen(game));
                break;
            case 2: // Settings
                // Pass 'this' as previousScreen so Back button returns here
                game.setScreen(new SettingsScreen(game, this));
                break;
            case 3: // Main Menu
                game.setScreen(new MenuScreen(game));
                break;
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); gameScreen.resize(width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        if (titleFont != null) titleFont.dispose();
        if (font != null) font.dispose();
    }
}
