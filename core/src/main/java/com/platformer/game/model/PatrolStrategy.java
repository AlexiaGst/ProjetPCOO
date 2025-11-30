package com.platformer.game.model;

import com.platformer.engine.model.IStrategy; // Import du Moteur
import com.platformer.engine.model.Agent;
import com.platformer.game.model.Enemy; // Import du Jeu

public class PatrolStrategy implements IStrategy {

    @Override
    public void execute(Agent agent, float deltaTime) {
        // On doit caster l'Agent en Enemy pour accéder aux méthodes spécifiques (comme reverseDirection)
        // C'est sûr de le faire car on sait qu'on l'attribuera à des ennemis.
        if (!(agent instanceof Enemy)) return;

        Enemy enemy = (Enemy) agent;

        // ... Logique identique à avant ...
        if (Math.abs(enemy.getVelocityX()) < 0.1f) {
            enemy.reverseDirection();
        }

        // Note : assure-toi que getConfiguredSpeed() est accessible
        float speed = enemy.getConfiguredSpeed();
        if (!enemy.isFacingRight()) speed = -speed;

        enemy.setVelocityX(speed);
    }
}
