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
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport; // Import
import com.badlogic.gdx.utils.viewport.Viewport;     // Import
import com.arcane.onslaught.upgrades.*;
import com.arcane.onslaught.utils.Constants;

import java.util.List;

public class UpgradeScreen implements Screen {
    private Game game;
    private GameScreen gameScreen;
    private OrthographicCamera camera;
    private Viewport viewport; // --- NEW ---
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont titleFont;
    private BitmapFont font;
    private BitmapFont smallFont;
    private GlyphLayout layout;

    private List<Upgrade> upgradeOptions;
    private int selectedIndex = 0;
    private float blinkTimer = 0;

    public UpgradeScreen(Game game, GameScreen gameScreen, List<Upgrade> upgradeOptions) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.upgradeOptions = upgradeOptions;
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
        titleFont.getData().setScale(3f);
        titleFont.setColor(Color.GOLD);

        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);

        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.3f);
        smallFont.setColor(Color.LIGHT_GRAY);
    }

    @Override
    public void render(float delta) {
        // Toggle Fullscreen
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode((int)Constants.SCREEN_WIDTH, (int)Constants.SCREEN_HEIGHT);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        blinkTimer += delta;
        camera.update();

        // Draw game underneath (frozen)
        gameScreen.renderPaused(delta);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.8f);
        shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        shapeRenderer.end();

        drawUpgradeBoxes();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        layout.setText(titleFont, "LEVEL UP!");
        titleFont.draw(batch, "LEVEL UP!", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT - 80f);

        font.getData().setScale(1.5f);
        layout.setText(font, "Choose an upgrade:");
        font.draw(batch, "Choose an upgrade:", Constants.SCREEN_WIDTH / 2f - layout.width / 2, Constants.SCREEN_HEIGHT - 150f);

        drawUpgradeText();

        smallFont.getData().setScale(1.2f);
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "1, 2, 3 - Select    ENTER - Confirm", 50f, 50f);

        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        handleInput();
    }

    private void drawUpgradeBoxes() {
        float boxWidth = 350f;
        float boxHeight = 220f;
        float spacing = 30f;
        float startX = (Constants.SCREEN_WIDTH - (boxWidth * 3 + spacing * 2)) / 2f;
        float y = Constants.SCREEN_HEIGHT / 2f - boxHeight / 2f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < upgradeOptions.size() && i < 3; i++) {
            float x = startX + i * (boxWidth + spacing);
            if (i == selectedIndex) {
                float pulse = 0.7f + 0.3f * (float)Math.sin(blinkTimer * 4);
                shapeRenderer.setColor(0.8f * pulse, 0.6f * pulse, 0f, 0.9f);
            } else {
                shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 0.9f);
            }
            shapeRenderer.rect(x, y, boxWidth, boxHeight);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);

        for (int i = 0; i < upgradeOptions.size() && i < 3; i++) {
            float x = startX + i * (boxWidth + spacing);
            Upgrade upgrade = upgradeOptions.get(i);

            Color rarityColor = getRarityColor(upgrade.getRarity());
            shapeRenderer.setColor(rarityColor);
            shapeRenderer.rect(x, y, boxWidth, boxHeight);

            if (i == selectedIndex) {
                shapeRenderer.rect(x - 3, y - 3, boxWidth + 6, boxHeight + 6);
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawUpgradeText() {
        float boxWidth = 350f;
        float boxHeight = 220f;
        float spacing = 30f;
        float startX = (Constants.SCREEN_WIDTH - (boxWidth * 3 + spacing * 2)) / 2f;
        float y = Constants.SCREEN_HEIGHT / 2f;

        for (int i = 0; i < upgradeOptions.size() && i < 3; i++) {
            float x = startX + i * (boxWidth + spacing) + 20f;
            Upgrade upgrade = upgradeOptions.get(i);

            font.getData().setScale(2.5f);
            font.setColor(Color.WHITE);
            font.draw(batch, String.valueOf(i + 1), x, y + 90f);

            font.getData().setScale(1.5f);
            Color rarityColor = getRarityColor(upgrade.getRarity());
            font.setColor(rarityColor);
            drawWrappedText(font, upgrade.getName(), x + 50f, y + 90f, boxWidth - 70f, 20f);

            smallFont.getData().setScale(1.1f);
            smallFont.setColor(Color.LIGHT_GRAY);
            drawWrappedText(smallFont, upgrade.getDescription(), x, y + 40f, boxWidth - 40f, 22f);

            smallFont.getData().setScale(1f);
            smallFont.setColor(rarityColor);
            String rarityText = upgrade.getRarity().toString();
            smallFont.draw(batch, rarityText, x, y - 70f);
        }
    }

    private void drawWrappedText(BitmapFont fontToUse, String text, float x, float y, float maxWidth, float lineHeight) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float lineY = y;

        for (String word : words) {
            String testLine = line.toString() + word + " ";
            layout.setText(fontToUse, testLine);
            if (layout.width > maxWidth && line.length() > 0) {
                fontToUse.draw(batch, line.toString().trim(), x, lineY);
                lineY -= lineHeight;
                line = new StringBuilder(word + " ");
            } else {
                line.append(word).append(" ");
            }
        }
        if (line.length() > 0) {
            fontToUse.draw(batch, line.toString().trim(), x, lineY);
        }
    }

    private Color getRarityColor(Upgrade.UpgradeRarity rarity) {
        switch (rarity) {
            case COMMON: return new Color(0.8f, 0.8f, 0.8f, 1f);
            case UNCOMMON: return new Color(0.2f, 1f, 0.2f, 1f);
            case RARE: return new Color(0.3f, 0.6f, 1f, 1f);
            case EPIC: return new Color(1f, 0.3f, 1f, 1f);
            default: return Color.GRAY;
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) selectedIndex = 0;
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && upgradeOptions.size() > 1) selectedIndex = 1;
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && upgradeOptions.size() > 2) selectedIndex = 2;

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) selectedIndex = Math.max(0, selectedIndex - 1);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) selectedIndex = Math.min(upgradeOptions.size() - 1, selectedIndex + 1);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && selectedIndex < upgradeOptions.size()) {
            selectUpgrade(upgradeOptions.get(selectedIndex));
        }
    }

    private void selectUpgrade(Upgrade chosen) {
        gameScreen.applyUpgrade(chosen);
        game.setScreen(gameScreen);
    }

    @Override
    public void resize(int width, int height) {
        // --- NEW: Resize BOTH Viewports ---
        viewport.update(width, height, true);
        gameScreen.resize(width, height);
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
        smallFont.dispose();
        shapeRenderer.dispose();
    }
}
