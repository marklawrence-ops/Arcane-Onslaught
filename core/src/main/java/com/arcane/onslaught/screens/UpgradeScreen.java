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
import com.arcane.onslaught.upgrades.*;
import com.arcane.onslaught.utils.Constants;

import java.util.List;

/**
 * Screen shown when player levels up - choose 1 of 3 upgrades
 */
public class UpgradeScreen implements Screen {
    private Game game;
    private GameScreen gameScreen;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont titleFont;
    private BitmapFont font;
    private BitmapFont smallFont;

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
        camera = new OrthographicCamera(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        camera.setToOrtho(false);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

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
        blinkTimer += delta;

        // Draw game underneath (frozen)
        gameScreen.renderPaused(delta);

        // Dark overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.8f);
        shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        shapeRenderer.end();

        // Draw upgrade boxes
        drawUpgradeBoxes();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Draw text
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        String title = "LEVEL UP!";
        titleFont.draw(batch, title, Constants.SCREEN_WIDTH / 2f - 120f, Constants.SCREEN_HEIGHT - 80f);

        // Instructions
        font.getData().setScale(1.5f);
        font.draw(batch, "Choose an upgrade:", Constants.SCREEN_WIDTH / 2f - 150f, Constants.SCREEN_HEIGHT - 150f);

        // Draw upgrade options
        drawUpgradeText();

        // Controls
        smallFont.draw(batch, "1, 2, 3 - Select    ENTER - Confirm", 50f, 50f);

        batch.end();

        // Handle input
        handleInput();
    }

    private void drawUpgradeBoxes() {
        float boxWidth = 350f;
        float boxHeight = 200f;
        float spacing = 30f;
        float startX = (Constants.SCREEN_WIDTH - (boxWidth * 3 + spacing * 2)) / 2f;
        float y = Constants.SCREEN_HEIGHT / 2f - boxHeight / 2f;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < upgradeOptions.size() && i < 3; i++) {
            float x = startX + i * (boxWidth + spacing);
            Upgrade upgrade = upgradeOptions.get(i);

            // Background
            if (i == selectedIndex) {
                // Selected - glowing gold
                float pulse = 0.7f + 0.3f * (float)Math.sin(blinkTimer * 4);
                shapeRenderer.setColor(0.8f * pulse, 0.6f * pulse, 0f, 0.9f);
            } else {
                // Not selected - dark
                shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 0.9f);
            }
            shapeRenderer.rect(x, y, boxWidth, boxHeight);
        }

        shapeRenderer.end();

        // Borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < upgradeOptions.size() && i < 3; i++) {
            float x = startX + i * (boxWidth + spacing);
            Upgrade upgrade = upgradeOptions.get(i);

            // Border color based on rarity
            Color rarityColor = getRarityColor(upgrade.getRarity());
            shapeRenderer.setColor(rarityColor);
            shapeRenderer.rect(x, y, boxWidth, boxHeight);

            // Double border for selected
            if (i == selectedIndex) {
                shapeRenderer.rect(x - 2, y - 2, boxWidth + 4, boxHeight + 4);
            }
        }
        shapeRenderer.end();
    }

    private void drawUpgradeText() {
        float boxWidth = 350f;
        float boxHeight = 200f;
        float spacing = 30f;
        float startX = (Constants.SCREEN_WIDTH - (boxWidth * 3 + spacing * 2)) / 2f;
        float y = Constants.SCREEN_HEIGHT / 2f;

        for (int i = 0; i < upgradeOptions.size() && i < 3; i++) {
            float x = startX + i * (boxWidth + spacing) + 20f;
            Upgrade upgrade = upgradeOptions.get(i);

            // Number
            font.getData().setScale(2.5f);
            font.setColor(Color.WHITE);
            font.draw(batch, String.valueOf(i + 1), x, y + 80f);

            // Name
            font.getData().setScale(1.8f);
            Color rarityColor = getRarityColor(upgrade.getRarity());
            font.setColor(rarityColor);
            font.draw(batch, upgrade.getName(), x + 40f, y + 80f);

            // Description (word wrap)
            smallFont.setColor(Color.LIGHT_GRAY);
            drawWrappedText(upgrade.getDescription(), x, y + 40f, boxWidth - 40f);

            // Rarity
            smallFont.getData().setScale(1f);
            smallFont.setColor(rarityColor);
            String rarityText = upgrade.getRarity().toString();
            smallFont.draw(batch, rarityText, x, y - 60f);
        }
    }

    private void drawWrappedText(String text, float x, float y, float maxWidth) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float lineY = y;

        for (String word : words) {
            String testLine = line + word + " ";
            if (smallFont.getRegion().getRegionWidth() * smallFont.getData().scaleX * testLine.length() * 0.6f > maxWidth) {
                smallFont.draw(batch, line.toString(), x, lineY);
                lineY -= 25f;
                line = new StringBuilder(word + " ");
            } else {
                line.append(word).append(" ");
            }
        }
        smallFont.draw(batch, line.toString(), x, lineY);
    }

    private Color getRarityColor(Upgrade.UpgradeRarity rarity) {
        switch (rarity) {
            case COMMON: return Color.WHITE;
            case UNCOMMON: return Color.GREEN;
            case RARE: return Color.CYAN;
            case EPIC: return Color.MAGENTA;
            default: return Color.GRAY;
        }
    }

    private void handleInput() {
        // Number keys to select
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            selectedIndex = 0;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && upgradeOptions.size() > 1) {
            selectedIndex = 1;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && upgradeOptions.size() > 2) {
            selectedIndex = 2;
        }

        // Arrow keys
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            selectedIndex = Math.min(upgradeOptions.size() - 1, selectedIndex + 1);
        }

        // Confirm selection
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
        smallFont.dispose();
        shapeRenderer.dispose();
    }
}
