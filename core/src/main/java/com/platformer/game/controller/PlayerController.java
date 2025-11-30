package com.platformer.game.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.platformer.game.model.Player;

public class PlayerController {
    private Player player;

    public PlayerController(Player player) {
        this.player = player;
    }

    public void update(float delta) {
        float dx = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dx = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dx = 1;
        }
        player.move(dx, 0);

        // Exemple d'action spécifique
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && player.isOnGround()) {
            player.jump();
        }

    }
}
