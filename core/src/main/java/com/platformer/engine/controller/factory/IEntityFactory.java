package com.platformer.engine.controller.factory;

import com.badlogic.gdx.maps.MapObject;
import com.platformer.engine.model.Entity;

public interface IEntityFactory {
    /**
     * Crée une entité basée sur un objet Tiled
     * @param type Le string "type" lu dans Tiled (ex: "PlayerStart")
     * @param x Position X convertie en unités
     * @param y Position Y convertie en unités
     * @param properties L'objet brut pour lire d'autres données (vitesse, etc)
     * @return L'entité créée ou null
     */
    Entity createEntity(String type, float x, float y, MapObject properties);
}
