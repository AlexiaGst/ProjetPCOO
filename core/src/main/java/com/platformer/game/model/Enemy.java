package com.platformer.game.model;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.platformer.engine.model.Agent;
import com.platformer.engine.model.IHostile;
import com.platformer.engine.model.IStrategy;

import java.util.HashMap;

public class Enemy extends Agent implements IHostile {
    private IStrategy strategy;
    private float configuredSpeed;
    public Enemy(String textureKey, float x, float y, float width, float height, int hp, float speed, IStrategy strategy) {
        super(textureKey, x, y, width, height, hp);
        this.strategy = strategy;
        this.configuredSpeed = speed;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (strategy != null) {
            // On passe 'this', qui est un Agent valide
            strategy.execute(this, deltaTime);
        }
    }

    public void reverseDirection() {
        // Si on regardait à droite, on force la vitesse vers la gauche
        if (isFacingRight()) {
            this.velocityX = -configuredSpeed;
        }
        // Sinon, on force vers la droite
        else {
            this.velocityX = configuredSpeed;
        }
    }
    public float getConfiguredSpeed() {
        return configuredSpeed;
    }

    @Override
    public int getDamage() {
        return 1; // Un ennemi standard fait 1 point de dégât
    }
}
