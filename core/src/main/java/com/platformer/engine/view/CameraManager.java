package com.platformer.engine.view;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CameraManager {

    private OrthographicCamera camera;
    private ViewportManager viewportManager;
    // Limites du monde (pour le clamping)
    private float mapWidth;
    private float mapHeight;

    public CameraManager() {
        this.camera = new OrthographicCamera();
    }

    public void setWorldBounds(float mapWidth, float mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    /**
     * Fait suivre une position cible par la caméra en restant dans les limites
     */
    public void update(float targetX, float targetY) {
        float y = MathUtils.clamp(targetY, camera.viewportHeight/2, mapHeight - camera.viewportHeight/2);
        // 1. Centrer grossièrement sur la cible
        camera.position.set(targetX, y, 0);

        // 2. CLAMPING (La logique mathématique est cachée ici)
        // On calcule la moitié de la vue actuelle (zoom inclus)
        float halfWidth = camera.viewportWidth / 2f  * camera.zoom;
        float halfHeight = (camera.viewportHeight * camera.zoom) / 2f;

        // On empêche de sortir à gauche/droite
        // On s'assure aussi que si la map est plus petite que l'écran, on reste centré (Math.max)
        float minX = halfWidth;
        float maxX = Math.max(minX, mapWidth - halfWidth);

        // On empêche de sortir en bas/haut
        float minY = halfHeight;
        float maxY = Math.max(minY, mapHeight - halfHeight);

        camera.position.x = MathUtils.clamp(camera.position.x, minX, maxX);
        camera.position.y = MathUtils.clamp(camera.position.y, minY, maxY);

        // 3. Appliquer les changements
        camera.update();
    }

    public void resize(ViewportManager vm, int width, int height) {
        vm.update(width, height);
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
