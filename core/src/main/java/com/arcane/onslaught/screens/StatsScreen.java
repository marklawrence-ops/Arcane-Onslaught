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
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.spells.Spell;
import com.arcane.onslaught.spells.SpellManager;
import com.arcane.onslaught.upgrades.PlayerBuild;
import com.arcane.onslaught.upgrades.Upgrade;
import com.arcane.onslaught.utils.Constants;
import com.arcane.onslaught.utils.FontManager; // Import

import java.util.Map;

public class StatsScreen implements Screen {
    private Game game;
    private GameScreen gameScreen;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    // Custom Fonts
    private BitmapFont titleFont;
    private BitmapFont headerFont;
    private BitmapFont font;

    private GlyphLayout layout;

    private Entity player;
    private PlayerBuild playerBuild;
    private SpellManager spellManager;

    public StatsScreen(Game game, GameScreen gameScreen, Entity player, PlayerBuild build, SpellManager spellManager) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.player = player;
        this.playerBuild = build;
        this.spellManager = spellManager;
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

        // --- FONT SETUP ---
        FontManager.getInstance().load();
        titleFont = generateFont("fonts/DungeonFont.ttf",60, Color.GOLD);
        headerFont = FontManager.getInstance().generateFont(38, Color.CYAN);
        font = FontManager.getInstance().generateFont(28, Color.WHITE);
        // ------------------
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
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode((int)Constants.SCREEN_WIDTH, (int)Constants.SCREEN_HEIGHT);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(gameScreen);
            return;
        }

        camera.update();
        gameScreen.renderPaused(delta);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.85f);
        shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(50, 50, Constants.SCREEN_WIDTH - 100, Constants.SCREEN_HEIGHT - 100);

        float col1 = Constants.SCREEN_WIDTH * 0.33f;
        float col2 = Constants.SCREEN_WIDTH * 0.66f;
        shapeRenderer.line(col1, 50, col1, Constants.SCREEN_HEIGHT - 120);
        shapeRenderer.line(col2, 50, col2, Constants.SCREEN_HEIGHT - 120);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        layout.setText(titleFont, "PLAYER STATS");
        titleFont.draw(batch, "PLAYER STATS", (Constants.SCREEN_WIDTH - layout.width)/2, Constants.SCREEN_HEIGHT - 60f);

        float startY = Constants.SCREEN_HEIGHT - 150f;
        float lineHeight = 30f;

        // --- COL 1: ATTRIBUTES ---
        headerFont.draw(batch, "ATTRIBUTES", 80f, startY);
        float y = startY - 50f;

        HealthComponent hp = player.getComponent(HealthComponent.class);
        if (hp != null) drawStat(batch, "Max Health", String.format("%.0f", hp.maxHealth), 80f, y -= lineHeight);

        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        if (vel != null) drawStat(batch, "Move Speed", String.format("%.0f", vel.maxSpeed), 80f, y -= lineHeight);

        ArmorComponent armor = player.getComponent(ArmorComponent.class);
        if (armor != null) drawStat(batch, "Armor", String.format("+%.0f%%", armor.damageReduction * 100), 80f, y -= lineHeight);

        RegenerationComponent regen = player.getComponent(RegenerationComponent.class);
        if (regen != null) drawStat(batch, "Regen", String.format("%.1f HP/s", regen.healAmount), 80f, y -= lineHeight);

        CriticalComponent crit = player.getComponent(CriticalComponent.class);
        if (crit != null) {
            drawStat(batch, "Crit Chance", String.format("%.0f%%", crit.critChance * 100), 80f, y -= lineHeight);
            drawStat(batch, "Crit Dmg", String.format("%.1fx", crit.critMultiplier), 80f, y -= lineHeight);
        }

        // --- COL 2: SPELLS ---
        headerFont.draw(batch, "SPELLS", col1 + 40f, startY);
        y = startY - 50f;

        for (Spell spell : spellManager.getActiveSpells()) {
            font.setColor(Color.YELLOW);
            font.draw(batch, spell.getName(), col1 + 40f, y);

            font.setColor(Color.GRAY);
            String stats = String.format("Dmg: %.0f | CD: %.2fs", spell.getDamage(), spell.getCooldown());
            font.draw(batch, stats, col1 + 40f, y - 20f);

            y -= 50f;
        }

        // --- COL 3: UPGRADES ---
        headerFont.draw(batch, "UPGRADES", col2 + 40f, startY);
        y = startY - 50f;

        Map<String, Integer> stacks = playerBuild.getUpgradeStackMap();
        for (Map.Entry<String, Integer> entry : stacks.entrySet()) {
            String name = entry.getKey();
            int count = entry.getValue();
            if (name.startsWith("Unlock")) continue;

            Color rarityColor = Color.WHITE;
            for (Upgrade u : playerBuild.getUpgrades()) {
                if (u.getName().equals(name)) {
                    rarityColor = getRarityColor(u.getRarity());
                    break;
                }
            }

            font.setColor(rarityColor);
            font.draw(batch, name, col2 + 40f, y);
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "x" + count, col2 + 250f, y);
            y -= 30f;
        }

        font.setColor(Color.GRAY);
        font.draw(batch, "[TAB] or [ESC] to Resume", (Constants.SCREEN_WIDTH)/2f - 100f, 40f);

        batch.end();
    }

    private void drawStat(SpriteBatch b, String label, String value, float x, float y) {
        font.setColor(Color.LIGHT_GRAY);
        font.draw(b, label + ":", x, y);
        font.setColor(Color.WHITE);
        font.draw(b, value, x + 140f, y);
    }

    private Color getRarityColor(Upgrade.UpgradeRarity rarity) {
        switch (rarity) {
            case COMMON: return new Color(0.8f, 0.8f, 0.8f, 1f);
            case UNCOMMON: return new Color(0.2f, 1f, 0.2f, 1f);
            case RARE: return new Color(0.3f, 0.6f, 1f, 1f);
            case EPIC: return new Color(0.8f, 0.3f, 1f, 1f);
            default: return Color.WHITE;
        }
    }

    @Override
    public void resize(int width, int height) {
        // --- FIX: Ensure centered resize ---
        viewport.update(width, height, true);
        gameScreen.resize(width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        if(titleFont!=null) titleFont.dispose();
        if(headerFont!=null) headerFont.dispose();
        if(font!=null) font.dispose();
    }
}
