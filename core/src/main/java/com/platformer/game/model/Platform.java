package com.platformer.game.model;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.platformer.engine.model.Artifact;

import java.util.HashMap;

public class Platform extends Artifact {
    public Platform(String textureKey, float x, float y, float width, float height) {
        super(textureKey, x, y, width, height);
    }
}
