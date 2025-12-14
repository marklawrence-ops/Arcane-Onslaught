package com.arcane.onslaught.screens;

import com.arcane.onslaught.enemies.EnemyFactory; // Import Factory
import com.arcane.onslaught.enemies.EnemyFactory.BossArchetype; // Import Archetypes
import com.arcane.onslaught.utils.SoundManager;
import com.badlogic.ashley.core.Entity;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.spells.SpellManager;
import com.arcane.onslaught.upgrades.*;
import com.arcane.onslaught.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class CheatMenuScreen implements Screen {
    private Game game;
    private GameScreen gameScreen;
    private Entity player;
    private PlayerBuild playerBuild;
    private SpellManager spellManager;
    private UpgradePool upgradePool;
    // --- NEW: Factory Reference ---
    private EnemyFactory enemyFactory;

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont smallFont;
    private GlyphLayout layout;

    private List<CheatButton> fixedButtons;
    private List<Upgrade> sortedUpgrades;
    private int currentPage = 0;
    private int totalPages = 0;
    private CheatButton nextBtn, prevBtn;
    private String hoverDescription = "";

    // --- NEW: Menu Modes ---
    private enum MenuMode { UPGRADES, SPAWNS }
    private MenuMode currentMode = MenuMode.UPGRADES;
    private CheatButton toggleModeBtn;
    // -----------------------

    private final int COLS = 3;
    private final int ROWS = 5;
    private final int ITEMS_PER_PAGE = COLS * ROWS;
    private Vector3 touchPoint;
    private CheatButton lastHoveredButton = null;

    private class CheatButton {
        String text;
        Rectangle bounds;
        Runnable action;
        Color color;
        Object userData;

        public CheatButton(String text, float x, float y, float w, float h, Runnable action) {
            this.text = text;
            this.bounds = new Rectangle(x, y, w, h);
            this.action = action;
            this.color = new Color(0.2f, 0.2f, 0.2f, 1f);
        }
    }

    // --- UPDATED CONSTRUCTOR ---
    public CheatMenuScreen(Game game, GameScreen gameScreen, Entity player, PlayerBuild build, SpellManager spellManager, UpgradePool upgradePool, EnemyFactory enemyFactory) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.player = player;
        this.playerBuild = build;
        this.spellManager = spellManager;
        this.upgradePool = upgradePool;
        this.enemyFactory = enemyFactory; // Store reference
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
        font = new BitmapFont();
        font.getData().setScale(1.2f);
        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.0f);

        layout = new GlyphLayout();
        touchPoint = new Vector3();

        sortedUpgrades = new ArrayList<>(upgradePool.getAllUpgrades());
        sortedUpgrades.sort((u1, u2) -> {
            int score1 = getCategoryScore(u1);
            int score2 = getCategoryScore(u2);
            if (score1 != score2) return Integer.compare(score1, score2);
            return u1.getName().compareTo(u2.getName());
        });

        calculatePages();
        createFixedButtons();
        createNavButtons();
    }

    private void calculatePages() {
        if (currentMode == MenuMode.UPGRADES) {
            totalPages = (int) Math.ceil((double) sortedUpgrades.size() / ITEMS_PER_PAGE);
        } else {
            // Boss list is short, fits on 1 page
            totalPages = 1;
        }
        if (currentPage >= totalPages) currentPage = 0;
    }

    private int getCategoryScore(Upgrade u) {
        if (u.getName().startsWith("Unlock")) return 1;
        if (u.getTags().contains("synergy")) return 2;
        return 3;
    }

    private void createFixedButtons() {
        fixedButtons = new ArrayList<>();
        float startY = Constants.SCREEN_HEIGHT - 60f;
        float btnWidth = 160f; // Smaller to fit Mode button
        float btnHeight = 40f;
        float spacing = 10f;
        float startX = (Constants.SCREEN_WIDTH - (5 * btnWidth + 4 * spacing)) / 2f;

        // 1. Mode Toggle
        toggleModeBtn = new CheatButton("Mode: UPGRADES", startX, startY, btnWidth, btnHeight, () -> {
            if (currentMode == MenuMode.UPGRADES) {
                currentMode = MenuMode.SPAWNS;
                toggleModeBtn.text = "Mode: SPAWNS";
                toggleModeBtn.color = Color.ORANGE;
            } else {
                currentMode = MenuMode.UPGRADES;
                toggleModeBtn.text = "Mode: UPGRADES";
                toggleModeBtn.color = new Color(0.2f, 0.2f, 0.2f, 1f);
            }
            calculatePages();
        });
        fixedButtons.add(toggleModeBtn);

        // 2. Standard Cheats
        fixedButtons.add(new CheatButton("Full Heal", startX + (btnWidth + spacing), startY, btnWidth, btnHeight, () -> {
            HealthComponent h = player.getComponent(HealthComponent.class);
            if (h != null) h.currentHealth = h.maxHealth;
        }));

        fixedButtons.add(new CheatButton("Level Up", startX + (btnWidth + spacing) * 2, startY, btnWidth, btnHeight, () -> {
            PlayerComponent pc = player.getComponent(PlayerComponent.class);
            if (pc != null) pc.xp += 1000;
        }));

        fixedButtons.add(new CheatButton("God Mode", startX + (btnWidth + spacing) * 3, startY, btnWidth, btnHeight, () -> {
            if (player.getComponent(GodModeComponent.class) == null) player.add(new GodModeComponent());
            else player.remove(GodModeComponent.class);
        }));

        fixedButtons.add(new CheatButton("Kill All", startX + (btnWidth + spacing) * 4, startY, btnWidth, btnHeight, () -> {
            System.out.println("Nuke pressed!");
        }));
    }

    private void createNavButtons() {
        float btnWidth = 100f;
        float btnHeight = 40f;
        float y = 80f;

        prevBtn = new CheatButton("< Prev", Constants.SCREEN_WIDTH / 2f - 120f, y, btnWidth, btnHeight, () -> {
            if (currentPage > 0) currentPage--;
        });

        nextBtn = new CheatButton("Next >", Constants.SCREEN_WIDTH / 2f + 20f, y, btnWidth, btnHeight, () -> {
            if (currentPage < totalPages - 1) currentPage++;
        });
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(gameScreen);
            return;
        }

        gameScreen.renderPaused(delta);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.9f);
        shapeRenderer.rect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

        handleInput();

        for (CheatButton btn : fixedButtons) drawButtonShape(btn);
        drawButtonShape(prevBtn);
        drawButtonShape(nextBtn);

        // --- DYNAMIC CONTENT BASED ON MODE ---
        List<CheatButton> pageButtons;
        if (currentMode == MenuMode.UPGRADES) {
            pageButtons = getUpgradeButtons(currentPage);
        } else {
            pageButtons = getBossSpawnButtons();
        }

        for (CheatButton btn : pageButtons) drawButtonShape(btn);

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        String title = (currentMode == MenuMode.UPGRADES) ? "UPGRADES" : "SPAWNER";
        font.setColor(Color.RED);
        font.draw(batch, "DEV MENU - " + title + " - PAGE " + (currentPage + 1) + "/" + totalPages, 20, Constants.SCREEN_HEIGHT - 10);

        GodModeComponent god = player.getComponent(GodModeComponent.class);
        font.setColor(god != null ? Color.GREEN : Color.GRAY);
        font.draw(batch, "GOD: " + (god != null ? "ON" : "OFF"), Constants.SCREEN_WIDTH - 150, Constants.SCREEN_HEIGHT - 10);

        for (CheatButton btn : fixedButtons) drawButtonText(btn, font);
        drawButtonText(prevBtn, font);
        drawButtonText(nextBtn, font);
        for (CheatButton btn : pageButtons) drawButtonText(btn, smallFont);

        if (!hoverDescription.isEmpty()) {
            font.setColor(Color.YELLOW);
            layout.setText(font, hoverDescription);
            font.draw(batch, hoverDescription, (Constants.SCREEN_WIDTH - layout.width) / 2f, 50f);
        }

        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // --- NEW: Generate Boss Spawn Buttons ---
    private List<CheatButton> getBossSpawnButtons() {
        List<CheatButton> list = new ArrayList<>();

        float startX = 200f;
        float startY = Constants.SCREEN_HEIGHT - 200f;
        float btnW = 300f;
        float btnH = 60f;
        float gapY = 30f;

        int i = 0;
        for (BossArchetype type : BossArchetype.values()) {
            float y = startY - i * (btnH + gapY);

            CheatButton btn = new CheatButton("Spawn " + type.name(), startX, y, btnW, btnH, () -> {
                // Spawn above player
                PositionComponent pc = player.getComponent(PositionComponent.class);
                Vector2 spawnPos = new Vector2(pc.position.x, pc.position.y + 300f);
                PlayerComponent playC = player.getComponent(PlayerComponent.class);

                enemyFactory.spawnBoss(gameScreen.getEngine(), spawnPos, playC.level, type);
                game.setScreen(gameScreen); // Resume game
            });

            // Color code
            switch (type) {
                case TITAN: btn.color = new Color(0.4f, 0.4f, 0.4f, 1f); break;
                case BERSERKER: btn.color = new Color(0.6f, 0.1f, 0.1f, 1f); break;
                case SPEEDSTER: btn.color = new Color(0.6f, 0.6f, 0.1f, 1f); break;
                case TANK: btn.color = new Color(0.1f, 0.4f, 0.1f, 1f); break;
            }

            list.add(btn);
            i++;
        }

        return list;
    }

    private List<CheatButton> getUpgradeButtons(int page) {
        List<CheatButton> list = new ArrayList<>();
        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, sortedUpgrades.size());

        float gridStartX = 100f;
        float gridStartY = Constants.SCREEN_HEIGHT - 150f;
        float cellW = 320f;
        float cellH = 50f;
        float gapX = 30f;
        float gapY = 20f;

        for (int i = start; i < end; i++) {
            Upgrade u = sortedUpgrades.get(i);
            int idxOnPage = i - start;
            int row = idxOnPage / COLS;
            int col = idxOnPage % COLS;

            float x = gridStartX + col * (cellW + gapX);
            float y = gridStartY - row * (cellH + gapY);

            CheatButton btn = new CheatButton(u.getName(), x, y, cellW, cellH, () -> {
                gameScreen.applyUpgrade(u);
            });
            btn.userData = u;

            if (playerBuild.hasUpgrade(u.getName()) || playerBuild.hasTag(u.getName())) {
                btn.color = new Color(0f, 0.4f, 0f, 1f);
            } else if (!u.canOffer(playerBuild, spellManager)) {
                btn.color = new Color(0.3f, 0.3f, 0.3f, 1f);
            }

            list.add(btn);
        }
        return list;
    }

    private void drawButtonShape(CheatButton btn) {
        shapeRenderer.setColor(btn.color);
        shapeRenderer.rect(btn.bounds.x, btn.bounds.y, btn.bounds.width, btn.bounds.height);
        shapeRenderer.setColor(Color.CYAN);
        float x = btn.bounds.x, y = btn.bounds.y, w = btn.bounds.width, h = btn.bounds.height;
        shapeRenderer.rectLine(x, y, x+w, y, 2);
        shapeRenderer.rectLine(x, y+h, x+w, y+h, 2);
        shapeRenderer.rectLine(x, y, x, y+h, 2);
        shapeRenderer.rectLine(x+w, y, x+w, y+h, 2);
    }

    private void drawButtonText(CheatButton btn, BitmapFont fontToUse) {
        fontToUse.setColor(Color.WHITE);
        layout.setText(fontToUse, btn.text);
        float tx = btn.bounds.x + (btn.bounds.width - layout.width) / 2;
        float ty = btn.bounds.y + (btn.bounds.height + layout.height) / 2;
        fontToUse.draw(batch, btn.text, tx, ty);
    }

    private void handleInput() {
        hoverDescription = "";
        boolean isTouched = Gdx.input.justTouched();
        camera.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

        CheatButton currentHover = null;

        List<CheatButton> activeButtons = new ArrayList<>(fixedButtons);
        activeButtons.add(prevBtn);
        activeButtons.add(nextBtn);

        if (currentMode == MenuMode.UPGRADES) activeButtons.addAll(getUpgradeButtons(currentPage));
        else activeButtons.addAll(getBossSpawnButtons());

        for (CheatButton btn : activeButtons) {
            if (btn.bounds.contains(touchPoint.x, touchPoint.y)) {
                currentHover = btn;
                btn.color = btn.color.cpy().add(0.1f, 0.1f, 0.1f, 0f);

                if (btn.userData instanceof Upgrade) {
                    hoverDescription = ((Upgrade) btn.userData).getDescription();
                }

                if (isTouched) {
                    SoundManager.getInstance().play("ui_click");
                    btn.action.run();
                }
            } else {
                if (btn.userData instanceof Upgrade) {
                    Upgrade u = (Upgrade) btn.userData;
                    if (playerBuild.hasUpgrade(u.getName()) || playerBuild.hasTag(u.getName())) {
                        btn.color = new Color(0f, 0.4f, 0f, 1f);
                    } else {
                        btn.color = new Color(0.2f, 0.2f, 0.2f, 1f);
                    }
                } else if (btn == toggleModeBtn && currentMode == MenuMode.SPAWNS) {
                    btn.color = Color.ORANGE;
                } else if (currentMode == MenuMode.SPAWNS && btn.text.startsWith("Spawn")) {
                    if(btn.text.contains("TITAN")) btn.color = new Color(0.4f, 0.4f, 0.4f, 1f);
                    else if(btn.text.contains("BERSERKER")) btn.color = new Color(0.6f, 0.1f, 0.1f, 1f);
                    else if(btn.text.contains("SPEEDSTER")) btn.color = new Color(0.6f, 0.6f, 0.1f, 1f);
                    else if(btn.text.contains("TANK")) btn.color = new Color(0.1f, 0.4f, 0.1f, 1f);
                } else {
                    btn.color = new Color(0.2f, 0.2f, 0.2f, 1f);
                }
            }
            lastHoveredButton = currentHover;
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); gameScreen.resize(width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); shapeRenderer.dispose(); font.dispose(); smallFont.dispose(); }
}
