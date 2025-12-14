package com.arcane.onslaught.screens;

import com.arcane.onslaught.utils.AlmanacData;
import com.arcane.onslaught.utils.AlmanacData.Category;
import com.arcane.onslaught.utils.AlmanacData.Entry;
import com.arcane.onslaught.utils.Constants;
import com.arcane.onslaught.utils.SoundManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.HashMap;
import java.util.Map;

public class AlmanacScreen implements Screen {
    private Game game;
    private Screen parentScreen;

    // Scene2D
    private Stage stage;
    private Skin skin;
    private Table contentTable;
    private Map<Category, TextButton> categoryButtons;

    // Visuals & Rendering
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Particle[] particles;
    private Category currentCategory = Category.SPELLS;

    // --- PARTICLE CLASS ---
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

    public AlmanacScreen(Game game, Screen parentScreen) {
        this.game = game;
        this.parentScreen = parentScreen;
        this.categoryButtons = new HashMap<>();
    }

    @Override
    public void show() {
        // 1. Setup Rendering
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        stage = new Stage(new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT), batch);
        Gdx.input.setInputProcessor(stage);

        // 2. Setup Particles
        particles = new Particle[100];
        for (int i = 0; i < particles.length; i++) particles[i] = new Particle();

        // 3. Setup Skin & Fonts
        createSkin();

        // 4. Build UI Layout
        buildUI();
    }

    private void createSkin() {
        skin = new Skin();

        // --- FIX: Register textures explicitly as Drawable.class ---
        skin.add("bg_dark", createDrawable(new Color(0f, 0f, 0f, 0.6f)), Drawable.class);
        skin.add("bg_light", createDrawable(new Color(1f, 1f, 1f, 0.1f)), Drawable.class);
        skin.add("bg_highlight", createDrawable(new Color(0.3f, 0.6f, 1f, 0.3f)), Drawable.class);
        // -----------------------------------------------------------

        // Generate Fonts
        BitmapFont titleFont = generateFont("fonts/DungeonFont.ttf", 60, Color.GOLD);
        BitmapFont headerFont = generateFont("fonts/DungeonFont.ttf", 32, Color.CYAN);
        BitmapFont textFont = generateFont("fonts/DungeonFont.ttf", 24, Color.WHITE);
        BitmapFont loreFont = generateFont("fonts/DungeonFont.ttf", 20, new Color(0.7f, 0.7f, 0.8f, 1f));

        skin.add("title", new Label.LabelStyle(titleFont, Color.GOLD));
        skin.add("header", new Label.LabelStyle(headerFont, Color.CYAN));
        skin.add("default", new Label.LabelStyle(textFont, Color.WHITE));
        skin.add("lore", new Label.LabelStyle(loreFont, Color.LIGHT_GRAY));

        // Button Style
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = textFont;
        btnStyle.up = skin.getDrawable("bg_dark");
        btnStyle.over = skin.getDrawable("bg_light");
        btnStyle.checked = skin.getDrawable("bg_highlight");
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = Color.YELLOW;
        skin.add("default", btnStyle);

        // ScrollPane Style
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = skin.getDrawable("bg_dark");
        skin.add("default", scrollStyle);
    }

    // Helper to create disposable drawable cleanly
    private Drawable createDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose(); // Good practice: dispose pixmap after texture creation
        return new TextureRegionDrawable(texture);
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.pad(20);

        // --- TITLE ---
        Label title = new Label("ARCHIVES OF THE ABYSS", skin, "title");
        title.setAlignment(Align.center);
        root.add(title).growX().colspan(2).padBottom(30).row();

        // --- LEFT PANEL: CATEGORIES ---
        Table sidePanel = new Table();
        sidePanel.top();

        for (Category cat : Category.values()) {
            TextButton btn = new TextButton(cat.name(), skin);
            btn.getLabel().setAlignment(Align.left);
            btn.pad(10, 20, 10, 20);

            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    SoundManager.getInstance().play("ui_click");
                    updateCategorySelection(cat);
                }
            });

            categoryButtons.put(cat, btn);
            sidePanel.add(btn).growX().height(60).padBottom(10).row();
        }

        // Return Button
        TextButton backBtn = new TextButton("RETURN", skin);
        backBtn.setColor(1f, 0.5f, 0.5f, 1f);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().play("ui_click");
                game.setScreen(parentScreen);
            }
        });
        sidePanel.add(backBtn).growX().height(60).padTop(50).row();

        root.add(sidePanel).width(250).top().padRight(20);

        // --- RIGHT PANEL: CONTENT ---
        contentTable = new Table();
        contentTable.top().left().pad(20);

        ScrollPane scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        root.add(scrollPane).grow();

        stage.addActor(root);

        // Initialize Selection
        updateCategorySelection(Category.SPELLS);
    }

    private void updateCategorySelection(Category cat) {
        // Update Buttons
        for (Map.Entry<Category, TextButton> entry : categoryButtons.entrySet()) {
            entry.getValue().setChecked(entry.getKey() == cat);
        }
        currentCategory = cat;

        // Refresh Content
        contentTable.clearChildren();
        java.util.List<Entry> entries = AlmanacData.getEntries(cat);

        for (Entry e : entries) {
            Table entryTable = new Table();
            entryTable.setBackground(skin.getDrawable("bg_light"));
            entryTable.pad(15);

            Table textTable = new Table();

            // Name
            Label nameLabel = new Label(e.name, skin, "header");
            textTable.add(nameLabel).left().growX().row();

            // Description
            Label descLabel = new Label(e.description, skin, "default");
            descLabel.setWrap(true);
            textTable.add(descLabel).left().growX().padTop(5).row();

            // Lore
            Label loreLabel = new Label(e.lore, skin, "lore");
            loreLabel.setWrap(true);
            loreLabel.setAlignment(Align.right);
            textTable.add(loreLabel).right().growX().padTop(10).row();

            entryTable.add(textTable).growX();
            contentTable.add(entryTable).growX().padBottom(10).row();
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
                parameter.borderWidth = 1;
                parameter.borderColor = Color.BLACK;
                parameter.shadowOffsetX = 2;
                parameter.shadowOffsetY = 2;
                parameter.shadowColor = new Color(0, 0, 0, 0.5f);
                font = generator.generateFont(parameter);
                generator.dispose();
            } catch (Exception e) {
                font = new BitmapFont();
            }
        } else {
            font = new BitmapFont();
        }
        font.setColor(color);
        return font;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);

        // Draw Particles (Background)
        stage.getViewport().apply();
        shapeRenderer.setProjectionMatrix(stage.getViewport().getCamera().combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Particle p : particles) {
            p.update(delta);
            shapeRenderer.setColor(p.color);
            shapeRenderer.circle(p.x, p.y, p.size);
        }
        shapeRenderer.end();

        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(parentScreen);
        }
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (skin != null) skin.dispose();
    }
}
