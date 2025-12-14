package com.arcane.onslaught.screens;

import com.arcane.onslaught.utils.HighscoreManager; // Import
import com.arcane.onslaught.utils.SoundManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.arcane.onslaught.utils.Constants;

public class MenuScreen implements Screen {
    private Game game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont subtitleFont;
    private BitmapFont font;
    private BitmapFont smallFont; // Added for highscore text
    private ShapeRenderer shapeRenderer;
    private GlyphLayout layout;

    private float time = 0;
    private Particle[] particles;

    private Vector3 touchPoint;
    private Rectangle startBounds;
    private Rectangle settingsBounds;
    private Rectangle almanacBounds;
    private boolean startHover = false;
    private boolean settingsHover = false;
    private boolean almanacHover = false;

    private static class Particle {
        float x, y, vx, vy, size;
        Color color;
        Particle() { reset(); }
        void reset() {
            x = (float)(Math.random() * Constants.SCREEN_WIDTH);
            y = (float)(Math.random() * Constants.SCREEN_HEIGHT);
            vx = (float)(Math.random() * 20 - 10);
            vy = (float)(Math.random() * 20 - 10);
            size = (float)(Math.random() * 3 + 1);
            float r = (float)Math.random();
            if (r < 0.33f) color = new Color(0.3f, 0.6f, 1f, 0.7f);
            else if (r < 0.66f) color = new Color(0.8f, 0.3f, 1f, 0.7f);
            else color = new Color(0.3f, 1f, 1f, 0.7f);
        }
        void update(float delta) {
            x += vx * delta;
            y += vy * delta;
            if (x < 0 || x > Constants.SCREEN_WIDTH || y < 0 || y > Constants.SCREEN_HEIGHT) reset();
        }
    }

    public MenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        SoundManager.getInstance().loadSounds();
        SoundManager.getInstance().play("fanfare");

        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
        viewport.apply();
        camera.position.set(Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        layout = new GlyphLayout();
        touchPoint = new Vector3();

        titleFont = generateFont("fonts/DungeonFont.ttf", 80, new Color(0.3f, 0.8f, 1f, 1f));
        subtitleFont = generateFont("fonts/DungeonFont.ttf", 40, new Color(1f, 0.5f, 1f, 1f));
        font = generateFont("fonts/DungeonFont.ttf", 40, Color.WHITE);
        smallFont = generateFont("fonts/DungeonFont.ttf", 24, Color.GOLD); // Highscore font

        float centerX = Constants.SCREEN_WIDTH / 2f;
        float centerY = Constants.SCREEN_HEIGHT / 2f;

        startBounds = new Rectangle(centerX - 150, centerY - 20, 300, 50);
        settingsBounds = new Rectangle(centerX - 100, centerY - 80, 200, 50);
        almanacBounds = new Rectangle(centerX - 100, centerY - 140, 200, 50);

        particles = new Particle[100];
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle();
        }
    }

    private BitmapFont generateFont(String path, int size, Color color) {
        FileHandle file = Gdx.files.internal(path);
        BitmapFont font;
        if (file.exists()) {
            try {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(file);
                FreeTypeFontParameter parameter = new FreeTypeFontParameter();
                parameter.size = size;
                parameter.borderWidth = 2;
                parameter.borderColor = Color.BLACK;
                parameter.shadowOffsetX = 3;
                parameter.shadowOffsetY = 3;
                parameter.shadowColor = new Color(0, 0, 0, 0.5f);
                font = generator.generateFont(parameter);
                generator.dispose();
            } catch (Exception e) {
                font = new BitmapFont();
                font.getData().setScale(size / 16f);
            }
        } else {
            font = new BitmapFont();
            font.getData().setScale(size / 16f);
        }
        font.setColor(color);
        return font;
    }

    @Override
    public void render(float delta) {
        time += delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) Gdx.graphics.setWindowedMode((int)Constants.SCREEN_WIDTH, (int)Constants.SCREEN_HEIGHT);
            else Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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

        titleFont.getData().setScale(1f);
        layout.setText(titleFont, "ARCANE ONSLAUGHT");
        titleFont.draw(batch, "ARCANE ONSLAUGHT", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT - 150f);

        subtitleFont.getData().setScale(1f);
        layout.setText(subtitleFont, "Survive the Endless Horde");
        subtitleFont.draw(batch, "Survive the Endless Horde", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT - 250f);

        // --- NEW: Highscore Display ---
        String bestRun = "BEST RUN\nLevel: " + HighscoreManager.getBestLevel() + "\nTime: " + HighscoreManager.formatTime(HighscoreManager.getBestTime());
        layout.setText(smallFont, bestRun);
        smallFont.draw(batch, bestRun, Constants.SCREEN_WIDTH - layout.width - 20, Constants.SCREEN_HEIGHT - 20);
        // ------------------------------

        float pulse = 1.0f + 0.1f * (float)Math.sin(time * 3);
        font.getData().setScale(pulse);
        font.setColor(startHover ? Color.YELLOW : new Color(0.3f, 1f, 0.3f, 1f));
        layout.setText(font, "START GAME");
        font.draw(batch, "START GAME", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f + 20);

        font.getData().setScale(1f);
        font.setColor(settingsHover ? Color.YELLOW : Color.WHITE);
        layout.setText(font, "SETTINGS");
        font.draw(batch, "SETTINGS", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f - 40);

        font.setColor(almanacHover ? Color.YELLOW : Color.WHITE);
        layout.setText(font, "ALMANAC");
        font.draw(batch, "ALMANAC", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT / 2f - 100);

        font.getData().setScale(0.7f);
        font.setColor(new Color(0.7f, 0.7f, 0.7f, 0.8f));
        font.draw(batch, "Controls:\nWASD - Move\nESC - Pause\nSpells cast automatically", 50f, 250f);

        font.setColor(new Color(0.5f, 0.5f, 0.5f, 0.6f));
        font.draw(batch, "v1.5 - Roguelike Survivor", 20f, 30f);

        batch.end();
        handleInput();
    }

    private void handleInput() {
        camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

        boolean nowStart = startBounds.contains(touchPoint.x, touchPoint.y);
        if (nowStart && !startHover) SoundManager.getInstance().play("ui_hover");
        startHover = nowStart;

        boolean nowSettings = settingsBounds.contains(touchPoint.x, touchPoint.y);
        if (nowSettings && !settingsHover) SoundManager.getInstance().play("ui_hover");
        settingsHover = nowSettings;

        boolean nowAlmanac = almanacBounds.contains(touchPoint.x, touchPoint.y);
        if (nowAlmanac && !almanacHover) SoundManager.getInstance().play("ui_hover");
        almanacHover = nowAlmanac;


        if (Gdx.input.justTouched()) {
            if (startHover) {
                SoundManager.getInstance().play("ui_click");
                game.setScreen(new GameScreen(game));
            } else if (settingsHover) {
                SoundManager.getInstance().play("ui_click");
                game.setScreen(new SettingsScreen(game, this));
            } else if (almanacHover){
                SoundManager.getInstance().play("ui_click");
                game.setScreen(new AlmanacScreen(game, this));
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            SoundManager.getInstance().play("ui_click");
            game.setScreen(new GameScreen(game));
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); titleFont.dispose(); subtitleFont.dispose(); font.dispose(); smallFont.dispose(); shapeRenderer.dispose(); }
}
