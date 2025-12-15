package com.arcane.onslaught.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.arcane.onslaught.upgrades.*;
import com.arcane.onslaught.utils.Constants;
import com.arcane.onslaught.utils.SoundManager;
import com.arcane.onslaught.utils.FontManager;

import java.util.List;

public class UpgradeScreen implements Screen {
    private Game game;
    private GameScreen gameScreen;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private BitmapFont titleFont;
    private BitmapFont font;
    private BitmapFont smallFont;

    private GlyphLayout layout;
    private List<Upgrade> upgradeOptions;
    private int selectedIndex = 0;
    private int lastSelectedIndex = -1;
    private float blinkTimer = 0;

    private Vector3 touchPoint;
    private Rectangle[] cardBounds;

    // --- LAYOUT CONSTANTS ---
    private final int MAX_CARDS = 5;
    // ------------------------

    public UpgradeScreen(Game game, GameScreen gameScreen, List<Upgrade> upgradeOptions) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.upgradeOptions = upgradeOptions;
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
        titleFont = generateFont("fonts/DungeonFont.ttf", 72, Color.GOLD);
        font = FontManager.getInstance().generateFont(36, Color.WHITE); // Slightly smaller for 5 cards
        smallFont = FontManager.getInstance().generateFont(28, Color.LIGHT_GRAY);

        cardBounds = new Rectangle[MAX_CARDS];
        for(int i=0; i<MAX_CARDS; i++) cardBounds[i] = new Rectangle();

        SoundManager.getInstance().play("levelup", 1.0f);
    }

    private BitmapFont generateFont(String path, int size, Color color) {
        FileHandle file = Gdx.files.internal(path);
        BitmapFont font;
        if (file.exists()) {
            try {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(file);
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) Gdx.graphics.setWindowedMode((int)Constants.SCREEN_WIDTH, (int)Constants.SCREEN_HEIGHT);
            else Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
        blinkTimer += delta;
        camera.update();
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
        titleFont.draw(batch, "LEVEL UP!", (Constants.SCREEN_WIDTH - layout.width)/2, Constants.SCREEN_HEIGHT - 50f);

        layout.setText(font, "Choose an upgrade:");
        font.setColor(Color.WHITE);
        font.draw(batch, "Choose an upgrade:", (Constants.SCREEN_WIDTH - layout.width)/2, Constants.SCREEN_HEIGHT - 120f);

        drawUpgradeText();

        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "1-5 Select    ENTER Confirm", 50f, 50f);

        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        handleInput();

        if (selectedIndex != lastSelectedIndex) {
            SoundManager.getInstance().play("ui_hover");
            lastSelectedIndex = selectedIndex;
        }
    }

    private void drawUpgradeBoxes() {
        // --- NEW DIMENSIONS FOR 5 CARDS ---
        // Total Width available ~1280.
        // 5 cards * 220 width = 1100.
        // 4 gaps * 20 spacing = 80.
        // Total used = 1180. Fits comfortably.
        float boxWidth = 220f;
        float boxHeight = 350f; // Taller to fit text
        float spacing = 20f;

        // Dynamic centering based on actual count (in case only 3 upgrades are available)
        int count = Math.min(upgradeOptions.size(), MAX_CARDS);
        float totalRowWidth = (count * boxWidth) + ((count - 1) * spacing);
        float startX = (Constants.SCREEN_WIDTH - totalRowWidth) / 2f;
        float y = Constants.SCREEN_HEIGHT / 2f - boxHeight / 2f;

        camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < count; i++) {
            float x = startX + i * (boxWidth + spacing);
            cardBounds[i].set(x, y, boxWidth, boxHeight);

            if (cardBounds[i].contains(touchPoint.x, touchPoint.y)) {
                if (selectedIndex != i) selectedIndex = i;
                if (Gdx.input.justTouched()) selectUpgrade(upgradeOptions.get(i));
            }

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
        Gdx.gl.glLineWidth(3);
        for (int i = 0; i < count; i++) {
            float x = startX + i * (boxWidth + spacing);
            shapeRenderer.setColor(getRarityColor(upgradeOptions.get(i).getRarity()));
            shapeRenderer.rect(x, y, boxWidth, boxHeight);
            if (i == selectedIndex) shapeRenderer.rect(x - 3, y - 3, boxWidth + 6, boxHeight + 6);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawUpgradeText() {
        float boxWidth = 220f;
        float boxHeight = 350f;
        float spacing = 20f;

        int count = Math.min(upgradeOptions.size(), MAX_CARDS);
        float totalRowWidth = (count * boxWidth) + ((count - 1) * spacing);
        float startX = (Constants.SCREEN_WIDTH - totalRowWidth) / 2f;

        float cardY = Constants.SCREEN_HEIGHT / 2f - boxHeight / 2f;
        float nameY = cardY + boxHeight - 20f;
        float descY = nameY - 70f; // More space for title wrapping

        for (int i = 0; i < count; i++) {
            float x = startX + i * (boxWidth + spacing);
            Upgrade upgrade = upgradeOptions.get(i);
            Color rarityColor = getRarityColor(upgrade.getRarity());

            // 1. Number
            font.setColor(Color.WHITE);
            font.draw(batch, String.valueOf(i + 1), x + 10f, nameY);

            // 2. Name
            font.setColor(rarityColor);
            float nameWidth = boxWidth - 40f;
            font.draw(batch, upgrade.getName(), x + 30f, nameY, nameWidth, Align.topLeft, true);

            // 3. Description
            smallFont.setColor(Color.LIGHT_GRAY);
            float descWidth = boxWidth - 30f;
            smallFont.draw(batch, upgrade.getDescription(), x + 15f, descY, descWidth, Align.topLeft, true);

            // 4. Rarity (Bottom Center)
            smallFont.setColor(rarityColor);
            layout.setText(smallFont, upgrade.getRarity().toString());
            smallFont.draw(batch, upgrade.getRarity().toString(), x + (boxWidth - layout.width)/2, cardY + 30f);
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
        // --- ADDED KEYS FOR 4 AND 5 ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) selectedIndex = 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && upgradeOptions.size() > 1) selectedIndex = 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && upgradeOptions.size() > 2) selectedIndex = 2;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) && upgradeOptions.size() > 3) selectedIndex = 3;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5) && upgradeOptions.size() > 4) selectedIndex = 4;

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) selectedIndex = Math.max(0, selectedIndex - 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) selectedIndex = Math.min(upgradeOptions.size() - 1, selectedIndex + 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && selectedIndex < upgradeOptions.size()) selectUpgrade(upgradeOptions.get(selectedIndex));
    }

    private void selectUpgrade(Upgrade chosen) {
        SoundManager.getInstance().play("ui_click");
        gameScreen.applyUpgrade(chosen);
        game.setScreen(gameScreen);
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); gameScreen.resize(width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        batch.dispose();
        if(titleFont!=null) titleFont.dispose();
        if(font!=null) font.dispose();
        if(smallFont!=null) smallFont.dispose();
        shapeRenderer.dispose();
    }
}
