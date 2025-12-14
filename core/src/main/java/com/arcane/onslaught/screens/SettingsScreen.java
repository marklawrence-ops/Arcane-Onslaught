package com.arcane.onslaught.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
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
import com.arcane.onslaught.utils.FontManager; // Import

public class SettingsScreen implements Screen {
    private Game game;
    private Screen previousScreen;

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GlyphLayout layout;
    private Vector3 touchPoint;

    private Rectangle masterBar, sfxBar, musicBar, backBtn;
    private boolean backHover = false;

    public SettingsScreen(Game game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);

        // --- FIX: Center Camera ---
        viewport.apply();
        camera.position.set(Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f, 0);
        camera.update();
        // --------------------------

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        layout = new GlyphLayout();
        touchPoint = new Vector3();

        // --- FONT SETUP ---
        FontManager.getInstance().load();
        font = FontManager.getInstance().generateFont(32, Color.WHITE);
        // ------------------

        float centerX = Constants.SCREEN_WIDTH / 2f;
        float startY = Constants.SCREEN_HEIGHT / 2f + 50f;
        float barW = 400f;
        float barH = 30f;

        masterBar = new Rectangle(centerX - barW/2, startY, barW, barH);
        sfxBar = new Rectangle(centerX - barW/2, startY - 80, barW, barH);
        musicBar = new Rectangle(centerX - barW/2, startY - 160, barW, barH);
        backBtn = new Rectangle(centerX - 100, startY - 250, 200, 50);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        handleInput();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawSlider(masterBar, SoundManager.getInstance().getMasterVolume(), Color.CYAN);
        drawSlider(sfxBar, SoundManager.getInstance().getSFXVolume(), Color.GREEN);
        drawSlider(musicBar, SoundManager.getInstance().getMusicVolume(), Color.MAGENTA);

        shapeRenderer.setColor(backHover ? Color.GRAY : Color.DARK_GRAY);
        shapeRenderer.rect(backBtn.x, backBtn.y, backBtn.width, backBtn.height);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.setColor(Color.WHITE);
        drawLabel("Master Volume", masterBar);
        drawLabel("SFX Volume", sfxBar);
        drawLabel("Music Volume", musicBar);

        drawPercent(SoundManager.getInstance().getMasterVolume(), masterBar);
        drawPercent(SoundManager.getInstance().getSFXVolume(), sfxBar);
        drawPercent(SoundManager.getInstance().getMusicVolume(), musicBar);

        layout.setText(font, "BACK");
        font.setColor(backHover ? Color.YELLOW : Color.WHITE);
        font.draw(batch, "BACK", backBtn.x + (backBtn.width - layout.width)/2, backBtn.y + (backBtn.height + layout.height)/2);

        batch.end();
    }

    private void drawSlider(Rectangle bounds, float value, Color fill) {
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapeRenderer.setColor(fill);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width * value, bounds.height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(bounds.x + (bounds.width * value) - 5, bounds.y - 5, 10, bounds.height + 10);
    }

    private void drawLabel(String text, Rectangle bar) {
        layout.setText(font, text);
        font.draw(batch, text, bar.x, bar.y + bar.height + 35);
    }

    private void drawPercent(float value, Rectangle bar) {
        String p = (int)(value * 100) + "%";
        layout.setText(font, p);
        font.draw(batch, p, bar.x + bar.width + 20, bar.y + bar.height - 5);
    }

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            if (masterBar.contains(touchPoint.x, touchPoint.y) || (Gdx.input.isTouched() && touchPoint.y >= masterBar.y && touchPoint.y <= masterBar.y + masterBar.height)) {
                float val = (touchPoint.x - masterBar.x) / masterBar.width;
                SoundManager.getInstance().setMasterVolume(val);
            }
            else if (sfxBar.contains(touchPoint.x, touchPoint.y) || (Gdx.input.isTouched() && touchPoint.y >= sfxBar.y && touchPoint.y <= sfxBar.y + sfxBar.height)) {
                SoundManager.getInstance().setSFXVolume((touchPoint.x - sfxBar.x) / sfxBar.width);
            }
            else if (musicBar.contains(touchPoint.x, touchPoint.y) || (Gdx.input.isTouched() && touchPoint.y >= musicBar.y && touchPoint.y <= musicBar.y + musicBar.height)) {
                SoundManager.getInstance().setMusicVolume((touchPoint.x - musicBar.x) / musicBar.width);
            }

            if (backBtn.contains(touchPoint.x, touchPoint.y) && Gdx.input.justTouched()) {
                SoundManager.getInstance().play("ui_click");
                game.setScreen(previousScreen);
            }
        }

        camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        boolean nowHover = backBtn.contains(touchPoint.x, touchPoint.y);
        if (nowHover && !backHover) SoundManager.getInstance().play("ui_hover");
        backHover = nowHover;
    }

    @Override
    public void resize(int width, int height) {
        // --- FIX: Ensure centered resize ---
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        if(font != null) font.dispose();
    }
}
