package com.platformer.game.controller;

import com.badlogic.gdx.maps.MapObject;
import com.platformer.engine.controller.factory.IEntityFactory;
import com.platformer.engine.model.Entity;
import com.platformer.engine.model.IStrategy;
import com.platformer.game.model.*;

public class GameEntityFactory implements IEntityFactory {

    @Override
    public Entity createEntity(String type, float x, float y, MapObject properties) {
        System.out.println(type);
        if (type == null) return null;

        switch (type) {
            case "PlayerStart":
                // On crée le joueur (taille 1x1 tuile par défaut)
                return new Player("PlayerStart", x, y, 1, 1, 10);

            case "Enemy":
                System.out.println("enemy !");
                // --- LECTURE DES PROPRIÉTÉS TILED ---
                // 1. Vitesse (float)
                float speed = 2.0f; // Valeur par défaut
                if (properties.getProperties().containsKey("speed")) {
                    speed = properties.getProperties().get("speed", Float.class);
                }

                // 2. Points de Vie (int)
                int hp = 1;
                if (properties.getProperties().containsKey("hp")) {
                    hp = properties.getProperties().get("hp", Integer.class);
                }

                // 3. Stratégie (String -> Class)
                String stratName = "PATROL"; // Valeur par défaut
                if (properties.getProperties().containsKey("strategy")) {
                    stratName = properties.getProperties().get("strategy", String.class);
                }

                // 4. Choix de l'intelligence artificielle
                IStrategy strategyImpl;

                // On compare la String venant de Tiled
                switch (stratName) {
                    case "STATIC":
                        strategyImpl = new StaticStrategy();
                        break;
                    case "PATROL":
                    default:
                        // Par défaut, l'ennemi patrouille
                        strategyImpl = new PatrolStrategy();
                        break;
                }

                // --- CRÉATION DE L'OBJET ---
                // On passe 'strategyImpl' au constructeur
                return new Enemy("Enemy", x, y, 1f, 1f, hp, speed, strategyImpl);

            default:
                return null;
        }
    }
}
