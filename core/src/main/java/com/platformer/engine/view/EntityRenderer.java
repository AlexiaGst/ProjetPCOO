package com.platformer.engine.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.platformer.engine.model.Entity;

public abstract class EntityRenderer implements Disposable{
    // Chaque renderer spécifique implémentera sa propre façon de dessiner
    public abstract void render(Entity entity, SpriteBatch batch);
}
