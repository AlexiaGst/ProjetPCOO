package com.platformer.game.model;

import com.platformer.engine.model.Agent;


//TODO : Singleton ?

public class Player extends Agent {
    public Player(String textureKey, float x, float y, float width, float height, int hp) {
        super(textureKey, x, y, width, height, hp);
    }

    public void jump() {
        if (isOnGround()) {
            this.velocityY = 15f;
            setOnGround(false);
            setState("JUMP");
            notifyObservers("PLAYER_JUMP");
        }
    }

    public void takeDamage(int amount) {
        int hp = getHp()-amount;

        if (hp <= 0) {
            setState("DEAD");
            notifyObservers("PLAYER_DIED");
        } else {
            notifyObservers("PLAYER_HIT");
        }
    }

}
