package com.platformer.game.controller.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20; // Import pour le nettoyage d'écran
import com.badlogic.gdx.graphics.glutils.ShapeRenderer; // Indispensable pour voir les collisions
import com.platformer.GameEngine;
import com.platformer.engine.controller.LevelManager;
import com.platformer.engine.view.CameraManager;
import com.platformer.engine.model.Entity;
import com.platformer.engine.view.ViewportManager;
import com.platformer.game.controller.GameEntityFactory;
import com.platformer.game.controller.PlayerController;
import com.platformer.game.model.Player;
import com.platformer.game.view.EntityRenderer;

public class GameScreen implements Screen {

    private final GameEngine game;

    // Engine Components
    private LevelManager levelManager;
    private CameraManager cameraManager;
    private ViewportManager viewportManager;

    // Game Components
    private PlayerController controller;
    private Entity playerEntity;

    private ShapeRenderer shapeRenderer;

    public GameScreen(GameEngine game) {
        this.game = game;

        this.shapeRenderer = new ShapeRenderer();
        // 1. Init CameraManager (VUE)
        cameraManager = new CameraManager();

        // 2. Init LevelManager (MOTEUR)
        levelManager = new LevelManager("maps/map.tmx", new GameEntityFactory(), cameraManager.getCamera());

        float worldHeight = levelManager.getMapHeight();
        float currentRatio = (float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
        float worldWidth = worldHeight * currentRatio;
        viewportManager = new ViewportManager(worldWidth, worldHeight, cameraManager.getCamera());

        cameraManager.setWorldBounds(levelManager.getMapWidth(), levelManager.getMapHeight());

        // 3. Setup Renderers
        EntityRenderer playerRenderer = new EntityRenderer();
        playerRenderer.addAnimation("JUMP","free-pixel-art-tiny-hero-sprites/Pink_Monster/Pink_Monster_Jump_8.png",8,true);
        playerRenderer.addAnimation("RUN","free-pixel-art-tiny-hero-sprites/Pink_Monster/Pink_Monster_Run_6.png",6,true);
        playerRenderer.addAnimation("IDLE","free-pixel-art-tiny-hero-sprites/Pink_Monster/Pink_Monster_Idle_4.png",4,true);
        playerRenderer.addAnimation("DEATH","free-pixel-art-tiny-hero-sprites/Pink_Monster/Pink_Monster_Death_8.png",8,true);
        playerRenderer.addAnimation("ATTACK","free-pixel-art-tiny-hero-sprites/Pink_Monster/Pink_Monster_Attack2_6.png",6,true);
        playerRenderer.addAnimation("HURT","free-pixel-art-tiny-hero-sprites/Pink_Monster/Pink_Monster_Hurt_4.png",4,true);
        levelManager.registerRenderer("PlayerStart", playerRenderer);


        EntityRenderer dudeRenderer = new EntityRenderer();
        dudeRenderer.addAnimation("WALK","free-pixel-art-tiny-hero-sprites/Dude_Monster/Dude_Monster_Walk_6.png",6,true);
        dudeRenderer.addAnimation("RUN","free-pixel-art-tiny-hero-sprites/Dude_Monster/Dude_Monster_Run_6.png",6,true);
        dudeRenderer.addAnimation("IDLE","free-pixel-art-tiny-hero-sprites/Dude_Monster/Dude_Monster_Idle_4.png",4,true);
        dudeRenderer.addAnimation("DEATH","free-pixel-art-tiny-hero-sprites/Dude_Monster/Dude_Monster_Death_8.png",8,true);
        dudeRenderer.addAnimation("ATTACK","free-pixel-art-tiny-hero-sprites/Dude_Monster/Dude_Monster_Attack2_6.png",6,true);
        dudeRenderer.addAnimation("HURT","free-pixel-art-tiny-hero-sprites/Dude_Monster/Dude_Monster_Hurt_4.png",4,true);
        levelManager.registerRenderer("Enemy", dudeRenderer);


        // 4. Setup Controller
        findPlayerAndSetup();
    }

    private void findPlayerAndSetup() {
        for (Entity e : levelManager.getEntities()) {
            if (e instanceof Player) {
                this.playerEntity = e;
                this.controller = new PlayerController((Player) e);
                break;
            }
        }
    }

    @Override
    public void render(float delta) {
        // --- NETTOYAGE ÉCRAN ---
        Color bgColor = Color.valueOf("82aad1");
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Important pour effacer la frame précédente

        float safeDelta = Math.min(delta, 0.1f);

        // --- UPDATE (Logique) ---
        if (controller != null) controller.update(safeDelta);
        levelManager.update(safeDelta);

        // --- CAMERA (Vue) ---
        if (playerEntity != null) {
            cameraManager.update(playerEntity.getX(), playerEntity.getY());
        }

        // --- DRAW JEU (Rendu graphique) ---
        viewportManager.getViewport().apply();
        game.batch.setProjectionMatrix(cameraManager.getCamera().combined);
        levelManager.render(game.batch);

        shapeRenderer.setProjectionMatrix(cameraManager.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.GREEN);

        // AVANT : Tu ne dessinais que le joueur
        // if (playerEntity != null) shapeRenderer.rect(...)

        // MAINTENANT : On dessine TOUT CE QUI EST VIVANT (Agent)
        for (Entity e : levelManager.getEntities()) {

            // On dessine une boite verte autour de tout le monde
            shapeRenderer.rect(e.getX(), e.getY(), e.getWidth(), e.getHeight());

            // Optionnel : Une couleur différente pour les ennemis pour les repérer
            if (e instanceof com.platformer.game.model.Enemy) {
                shapeRenderer.setColor(Color.ORANGE);
                shapeRenderer.rect(e.getX(), e.getY(), e.getWidth(), e.getHeight());
                shapeRenderer.setColor(Color.GREEN); // Reset
            }
        }
        shapeRenderer.end();

    }

    @Override
    public void resize(int width, int height) {
        cameraManager.resize(viewportManager, width, height);
    }

    @Override
    public void dispose() {
        levelManager.dispose();
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
