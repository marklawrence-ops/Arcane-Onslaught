package com.arcane.onslaught.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport; // Import Viewport
import com.badlogic.gdx.utils.viewport.Viewport;     // Import Viewport
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.entities.systems.*;
import com.arcane.onslaught.events.EventManager;
import com.arcane.onslaught.events.GameOverEvent;
import com.arcane.onslaught.events.LevelUpEvent;
import com.arcane.onslaught.input.GameInputProcessor;
import com.arcane.onslaught.spells.*;
import com.arcane.onslaught.enemies.*;
import com.arcane.onslaught.upgrades.*;
import com.arcane.onslaught.utils.*;

public class GameScreen implements Screen {

    private Game game;
    private Engine engine;
    private OrthographicCamera camera;
    private Viewport viewport; // --- NEW: Handles resizing/fullscreen scaling ---
    private ShapeRenderer shapeRenderer;

    private SpriteBatch mainBatch;
    private SpriteBatch damageBatch;

    private Entity player;
    private float gameTime = 0;
    private GameInputProcessor inputProcessor;
    private SpellManager spellManager;
    private EnemyFactory enemyFactory;
    private UpgradePool upgradePool;
    private PlayerBuild playerBuild;

    private boolean isGameOver = false;
    private boolean isPaused = false;

    public GameScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        if (engine != null) return;

        TextureManager.getInstance().loadTextures();

        camera = new OrthographicCamera();
        // --- NEW: Initialize Viewport ---
        // This ensures the game world stays at 1200x680, regardless of screen resolution
        viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
        viewport.apply();

        // Center camera
        camera.position.set(Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f, 0);
        camera.update();

        shapeRenderer = new ShapeRenderer();
        mainBatch = new SpriteBatch();
        damageBatch = new SpriteBatch();

        engine = new Engine();
        spellManager = new SpellManager();
        enemyFactory = new EnemyFactory();
        upgradePool = new UpgradePool();
        playerBuild = new PlayerBuild();

        inputProcessor = new GameInputProcessor();
        Gdx.input.setInputProcessor(inputProcessor);

        engine.addSystem(new PlayerInputSystem(inputProcessor));
        engine.addSystem(new MovementSystem());
        engine.addSystem(new AISystem());
        engine.addSystem(new SpellCastSystem(spellManager, playerBuild));
        engine.addSystem(new ProjectileSystem());
        engine.addSystem(new EnemySpawnSystem(enemyFactory));
        engine.addSystem(new XPMagnetSystem(playerBuild));
        engine.addSystem(new PoisonSystem());
        engine.addSystem(new SlowSystem());
        engine.addSystem(new RegenerationSystem());
        engine.addSystem(new CollisionSystem(playerBuild));

        engine.addSystem(new RenderSystem(shapeRenderer, mainBatch, camera));
        engine.addSystem(new UISystem(shapeRenderer, camera));
        engine.addSystem(new DamageIndicatorSystem(damageBatch, camera));

        createPlayer();
        setupEventListeners();
    }

    private void createPlayer() {
        player = new Entity();
        float startX = Constants.SCREEN_WIDTH / 2f;
        float startY = Constants.SCREEN_HEIGHT / 2f;
        player.add(new PositionComponent(startX, startY));
        player.add(new VelocityComponent(Constants.PLAYER_SPEED));
        TextureManager tm = TextureManager.getInstance();
        if (tm.hasTexture("player")) {
            player.add(new VisualComponent(Constants.PLAYER_SIZE * 2f, Constants.PLAYER_SIZE * 2f, tm.getTexture("player")));
        } else {
            player.add(new VisualComponent(Constants.PLAYER_SIZE, Constants.PLAYER_SIZE, Color.CYAN));
        }
        player.add(new HealthComponent(Constants.PLAYER_MAX_HEALTH));
        PlayerComponent pc = new PlayerComponent();
        pc.xp = 0;
        pc.xpToNextLevel = Constants.XP_TO_LEVEL_BASE;
        pc.level = 1;
        player.add(pc);
        engine.addEntity(player);
    }

    private void setupEventListeners() {
        EventManager em = EventManager.getInstance();
        em.subscribe(GameOverEvent.class, event -> {
            if (!isGameOver) {
                isGameOver = true;
                game.setScreen(new GameOverScreen(game, event.getSurvivalTime(), event.getFinalLevel()));
            }
        });
        em.subscribe(LevelUpEvent.class, event -> {
            for (com.arcane.onslaught.spells.Spell spell : spellManager.getActiveSpells()) {
                spell.setDamage(spell.getDamage() * 1.02f);
                float newCooldown = spell.getCooldown() * 0.97f;
                if (newCooldown > 0.3f) spell.setCooldown(newCooldown);
            }
            showUpgradeScreen();
        });
    }

    @Override
    public void render(float delta) {
        // --- NEW: Fullscreen Toggle (F11) ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode((int)Constants.SCREEN_WIDTH, (int)Constants.SCREEN_HEIGHT);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !isGameOver) {
            game.setScreen(new PauseScreen(game, this));
            return;
        }

        camera.update();

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        drawBackground();

        if (!isGameOver && !isPaused) {
            gameTime += delta;

            mainBatch.setProjectionMatrix(camera.combined);
            damageBatch.setProjectionMatrix(camera.combined);

            engine.update(delta);
        }

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(
            Constants.ARENA_OFFSET_X,
            Constants.ARENA_OFFSET_Y,
            Constants.ARENA_WIDTH,
            Constants.ARENA_HEIGHT
        );
        shapeRenderer.end();

        if (!isGameOver) {
            HealthComponent health = player.getComponent(HealthComponent.class);
            if (health != null && !health.isAlive()) {

                if (playerBuild.hasTag("has_revive")) {
                    health.currentHealth = health.maxHealth * 0.5f;
                    playerBuild.removeTag("has_revive");
                    System.out.println(">>> REVIVED! <<<");
                    return;
                }

                PlayerComponent pc = player.getComponent(PlayerComponent.class);
                EventManager.getInstance().publish(new GameOverEvent(gameTime, pc.level));
                isGameOver = true;
            }
        }
    }

    private void drawBackground() {
        TextureManager tm = TextureManager.getInstance();
        mainBatch.setColor(Color.WHITE);
        mainBatch.setProjectionMatrix(camera.combined);

        if (tm.hasTexture("background")) {
            mainBatch.begin();
            mainBatch.draw(tm.getTexture("background"),
                Constants.ARENA_OFFSET_X, Constants.ARENA_OFFSET_Y,
                Constants.ARENA_WIDTH, Constants.ARENA_HEIGHT);
            mainBatch.end();
        } else {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.08f, 0.08f, 0.12f, 1f);
            shapeRenderer.rect(Constants.ARENA_OFFSET_X, Constants.ARENA_OFFSET_Y, Constants.ARENA_WIDTH, Constants.ARENA_HEIGHT);
            shapeRenderer.end();
        }
    }

    public void renderPaused(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        drawBackground();

        engine.getSystem(RenderSystem.class).update(0);
        engine.getSystem(UISystem.class).update(0);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(Constants.ARENA_OFFSET_X, Constants.ARENA_OFFSET_Y, Constants.ARENA_WIDTH, Constants.ARENA_HEIGHT);
        shapeRenderer.end();
    }

    private void showUpgradeScreen() {
        java.util.List<Upgrade> upgrades = upgradePool.getRandomUpgrades(playerBuild, spellManager, 3);
        if (!upgrades.isEmpty()) {
            game.setScreen(new UpgradeScreen(game, this, upgrades));
        }
    }

    public void applyUpgrade(Upgrade upgrade) {
        upgrade.apply(player, spellManager, playerBuild);
        playerBuild.addUpgrade(upgrade);
    }

    @Override
    public void resize(int width, int height) {
        // --- NEW: Update Viewport on Resize ---
        viewport.update(width, height, true);
    }

    @Override
    public void pause() { isPaused = true; }

    @Override
    public void resume() { isPaused = false; }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        mainBatch.dispose();
        damageBatch.dispose();
        TextureManager.getInstance().dispose();
        if (engine.getSystem(UISystem.class) != null) engine.getSystem(UISystem.class).dispose();
        if (engine.getSystem(DamageIndicatorSystem.class) != null) engine.getSystem(DamageIndicatorSystem.class).dispose();
        EventManager.getInstance().clear();
    }
}
