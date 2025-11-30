package com.platformer.engine.view;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ViewportManager {
    private Viewport viewport;

    public ViewportManager(float width, float height, Camera camera) {
        this.viewport = new ExtendViewport(width, height, camera);
    }

    public void update(int width, int height) {
        viewport.update(width,height,false);
    }
    public Viewport getViewport() {
        return viewport;
    }
}
