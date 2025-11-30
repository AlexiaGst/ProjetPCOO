package com.platformer.game.model;

import com.platformer.engine.model.IStrategy;
import com.platformer.engine.model.Agent;

public class StaticStrategy implements IStrategy {

    @Override
    public void execute(Agent agent, float deltaTime) {
        // Stratégie : On ne bouge pas horizontalement.

        // On force la vitesse X à 0 à chaque frame pour annuler toute inertie.
        agent.setVelocityX(0);

        // Note Importante :
        // On ne touche PAS à velocityY.
        // Ainsi, si l'ennemi apparaît en l'air ou si le sol se dérobe,
        // le CollisionManager lui appliquera quand même la gravité.
    }
}
