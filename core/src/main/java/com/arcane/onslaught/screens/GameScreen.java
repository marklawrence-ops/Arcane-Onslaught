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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.arcane.onslaught.entities.components.*;
import com.arcane.onslaught.entities.systems.*;
import com.arcane.onslaught.events.EventManager;
import com.arcane.onslaught.events.GameOverEvent;
import com.arcane.onslaught.events.LevelUpEvent;
import com.arcane.onslaught.input.GameInputProcessor;
import com.arcane.onslaught.spells.*;
import com.arcane.onslaught.enemies.*;
import com.arcane.onslaught.upgrades.*;
import com.arcane.onslaught.utils.Constants;

/**
 * Main game screen - where the gameplay happens
 */
public class GameScreen implements Screen {

    private Game game;
    private Engine engine;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;

    private Entity player;
    private float gameTime = 0;
    private GameInputProcessor inputProcessor;
    private SpellManager spellManager;
    private EnemyFactory enemyFactory;
    private UpgradePool upgradePool;
    private PlayerBuild playerBuild;

    private boolean isGameOver = false;
    private boolean isPaused = false;

    // Store game reference
    public GameScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Only initialize if not already initialized
        if (engine != null) return; // Already initialized, just resuming

        camera = new OrthographicCamera(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        camera.setToOrtho(false);
        camera.update();

        shapeRenderer = new ShapeRenderer();
        engine = new Engine();
        spellManager = new SpellManager();
        enemyFactory = new EnemyFactory();
        upgradePool = new UpgradePool();
        playerBuild = new PlayerBuild();

        inputProcessor = new GameInputProcessor();
        Gdx.input.setInputProcessor(inputProcessor);

        // Add systems
        engine.addSystem(new PlayerInputSystem(inputProcessor));
        engine.addSystem(new MovementSystem());
        engine.addSystem(new AISystem());
        engine.addSystem(new SpellCastSystem(spellManager)); // Pass spell manager
        engine.addSystem(new ProjectileSystem());
        engine.addSystem(new EnemySpawnSystem(enemyFactory)); // Pass enemy factory
        engine.addSystem(new XPMagnetSystem());
        engine.addSystem(new PoisonSystem()); // NEW: Poison DOT
        engine.addSystem(new SlowSystem());   // NEW: Slow effects
        engine.addSystem(new CollisionSystem());
        engine.addSystem(new RenderSystem(shapeRenderer, camera));
        engine.addSystem(new UISystem(shapeRenderer, camera));

        createPlayer();
        setupEventListeners();

        System.out.println("═══════════════════════════════════");
        System.out.println("GAME START!");
        System.out.println("ESC - Pause");
        System.out.println("═══════════════════════════════════");
    }

    private void createPlayer() {
        player = new Entity();

        float startX = Constants.SCREEN_WIDTH / 2f;
        float startY = Constants.SCREEN_HEIGHT / 2f;

        player.add(new PositionComponent(startX, startY));
        player.add(new VelocityComponent(Constants.PLAYER_SPEED));
        player.add(new VisualComponent(Constants.PLAYER_SIZE, Constants.PLAYER_SIZE, Color.CYAN));
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
                System.out.println("\n═══════════════════════════════════");
                System.out.println("GAME OVER!");
                System.out.println("═══════════════════════════════════\n");

                // Switch to game over screen
                game.setScreen(new GameOverScreen(game, event.getSurvivalTime(), event.getFinalLevel()));
            }
        });

        em.subscribe(LevelUpEvent.class, event -> {
            // Show upgrade screen
            showUpgradeScreen();

            int level = event.getNewLevel();

            if (level == 3 && !spellManager.hasSpell("Fireball")) {
                spellManager.addSpell(new FireballSpell());
            } else if (level == 5 && !spellManager.hasSpell("Lightning Bolt")) {
                spellManager.addSpell(new LightningBoltSpell());
            } else if (level == 7 && !spellManager.hasSpell("Ice Shard")) {
                spellManager.addSpell(new IceShardSpell());
            } else if (level == 10 && !spellManager.hasSpell("Arcane Missiles")) {
                spellManager.addSpell(new ArcaneMissilesSpell());
            } else if (level == 15 && !spellManager.hasSpell("Poison Dart")) {
                spellManager.addSpell(new PoisonDartSpell());
            }
        });
    }

    @Override
    public void render(float delta) {
        // Check for pause
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !isGameOver) {
            game.setScreen(new PauseScreen(game, this));
            return;
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Check for game over
        if (!isGameOver) {
            HealthComponent health = player.getComponent(HealthComponent.class);
            if (health != null && !health.isAlive()) {
                PlayerComponent pc = player.getComponent(PlayerComponent.class);
                EventManager.getInstance().publish(
                    new GameOverEvent(gameTime, pc.level)
                );
                isGameOver = true;
                return;
            }
        }

        // Update game
        if (!isGameOver && !isPaused) {
            gameTime += delta;
            engine.update(delta);
        }

        // Draw arena boundary
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
    }

    /**
     * Render without updating (for pause screen)
     */
    public void renderPaused(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Just render, don't update
        engine.getSystem(RenderSystem.class).update(0);
        engine.getSystem(UISystem.class).update(0);

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
    }

    /**
     * Show upgrade selection screen
     */
    private void showUpgradeScreen() {
        // Get 3 random upgrades
        java.util.List<Upgrade> upgrades = upgradePool.getRandomUpgrades(playerBuild, spellManager, 3);

        if (upgrades.isEmpty()) {
            System.out.println("No upgrades available - skipping");
            return;
        }

        game.setScreen(new UpgradeScreen(game, this, upgrades));
    }

    /**
     * Apply chosen upgrade
     */
    public void applyUpgrade(Upgrade upgrade) {
        upgrade.apply(player, spellManager, playerBuild);
        playerBuild.addUpgrade(upgrade);
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {
        isPaused = true;
    }

    @Override
    public void resume() {
        isPaused = false;
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        UISystem uiSystem = engine.getSystem(UISystem.class);
        if (uiSystem != null) {
            uiSystem.dispose();
        }
        EventManager.getInstance().clear();
    }
}
