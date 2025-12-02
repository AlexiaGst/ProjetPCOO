package com.platformer.engine.controller;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.platformer.engine.controller.factory.IEntityFactory;
import com.platformer.engine.model.Agent;
import com.platformer.engine.model.Entity;
import com.platformer.engine.model.IDamageable;
import com.platformer.engine.model.IHostile;
import com.platformer.engine.view.CollisionManager;
import com.platformer.engine.view.EntityRenderer; // Attention : Adapter si tu veux une interface générique ici
import com.platformer.game.model.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LevelManager implements Disposable {

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;

    // Échelle : combien d'unités pour 1 pixel (ex: 1/16)
    private float unitScale;

    // Liste de toutes les entités du jeu
    private List<Entity> entities;

    private Array<Polygon> obstacles;
    private CollisionManager collisionManager;

    // Le dictionnaire pour savoir comment dessiner qui (Registry)
    // Map<Type d'entité, Le Dessinateur>
    private HashMap<String, EntityRenderer> rendererRegistry;

    public LevelManager(String mapPath, IEntityFactory factory, OrthographicCamera camera) {
        this.camera = camera;
        this.entities = new ArrayList<>();
        this.rendererRegistry = new HashMap<>();
        this.obstacles = new Array<>();
        this.collisionManager = new CollisionManager();

        // 1. Charger la carte
        map = new TmxMapLoader().load(mapPath);

        // 2. Calculer l'échelle (Adaptation automatique)
        MapProperties props = map.getProperties();
        int tileWidth = props.get("tilewidth", Integer.class);
        // Si la tuile fait 16px, 1 unité = 16px. Donc 1 pixel = 1/16 unités.
        this.unitScale = 1f / (float)tileWidth;

        // On récupère la largeur/hauteur en tuiles
        int mapWidthInTiles = props.get("width", Integer.class);
        int mapHeightInTiles = props.get("height", Integer.class);

        // On convertit en "Unités Monde" (Mètres)
        // Si 1 tuile = 1 mètre (car unitScale = 1/tileWidth), alors la largeur = nombre de tuiles
        // Mais pour être robuste mathématiquement : nb_tuiles * taille_tuile * scale
        float worldWidth = mapWidthInTiles * tileWidth * unitScale;
        float worldHeight = mapHeightInTiles * tileWidth * unitScale;

        // ON ENVOIE L'INFO AU COLLISION MANAGER !
        collisionManager.setWorldBounds(worldWidth, worldHeight);

        // 3. Configurer le rendu de la map Tiled
        mapRenderer = new OrthogonalTiledMapRenderer(map, unitScale);

        // 4. Charger les entités depuis la couche "Entities" de Tiled
        loadCollisions();
        loadEntities(factory);
        linkEntities();
    }

    private void linkEntities() {
        // 1. Trouver le joueur
        Player player = null;
        for (Entity e : entities) {
            if (e instanceof Player) {
                player = (Player) e;
                break;
            }
        }
        if (player == null) return;

        // 2. Dire à tous les ennemis qui est le joueur
        for (Entity e : entities) {
            if (e instanceof com.platformer.game.model.Enemy) {
                ((com.platformer.game.model.Enemy) e).setTarget(player);
            }
        }
    }


    private void loadCollisions() {
        // 1. Récupérer le calque nommé "Collisions" dans Tiled
        // ATTENTION : Doit être exactement le même nom que dans Tiled
        MapLayer collisionLayer = map.getLayers().get("objects");

        if (collisionLayer == null) {
            System.out.println("ATTENTION: Pas de calque 'Collisions' trouvé dans la map !");
            return;
        }

        // 2. Parcourir tous les objets de ce calque
        for (MapObject object : collisionLayer.getObjects()) {
            Polygon scaledPoly = null;
            // 3. Vérifier si c'est bien un Rectangle (Outil carré dans Tiled)
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                float[] vertices = new float[] {
                    0, 0,
                    rect.width, 0,
                    rect.width, rect.height,
                    0, rect.height
                };

                scaledPoly = new Polygon(vertices);
                scaledPoly.setPosition(rect.x, rect.y);

            }
            // CAS 2 : C'est un Polygone (Outil Plume/Polygone Tiled)
            else if (object instanceof PolygonMapObject) {
                Polygon poly = ((PolygonMapObject) object).getPolygon();

                // On récupère les points et on copie
                float[] vertices = poly.getVertices().clone();
                scaledPoly = new Polygon(vertices);
                scaledPoly.setPosition(poly.getX(), poly.getY());
            }

            // APPLIQUER L'ÉCHELLE (UnitScale)
            if (scaledPoly != null) {
                scaledPoly.setScale(unitScale, unitScale);

                // Astuce : setScale scale depuis l'origine (0,0), il faut corriger la position
                scaledPoly.setPosition(scaledPoly.getX() * unitScale, scaledPoly.getY() * unitScale);

                obstacles.add(scaledPoly);
            }
        }
        System.out.println("Obstacles chargés (Polygones) : " + obstacles.size);
    }

    // N'oublie pas de mettre à jour le getter
    public Array<Polygon> getObstacles() {
        return obstacles;
    }

    private void loadEntities(IEntityFactory factory) {
        MapLayer objectLayer = map.getLayers().get("Entities");
        if (objectLayer == null) return;

        for (MapObject object : objectLayer.getObjects()) {
            // Lire les propriétés de base Tiled
            String type = object.getProperties().get("type", String.class);
            float x = object.getProperties().get("x", Float.class);
            float y = object.getProperties().get("y", Float.class);

            // IMPORTANT : Convertir les coordonnées pixels Tiled en Unités Monde
            float worldX = x * unitScale;
            float worldY = y * unitScale;

            // Création via la Factory
            Entity e = factory.createEntity(type, worldX, worldY, object);

            if (e != null) {
                if (e instanceof com.platformer.engine.model.Agent) {
                    ((com.platformer.engine.model.Agent) e).setWorldObstacles(this.obstacles);
                }
                entities.add(e);
            }
        }
    }


    public void update(float delta) {
        java.util.Iterator<Entity> iter = entities.iterator();
        while (iter.hasNext()) {
            Entity e = iter.next();

            // Si c'est un Agent mort depuis trop longtemps (animation finie)
            if (e instanceof Agent) {
                Agent a = (Agent) e;
                if (a.isDead()) {
                    // On peut vérifier le stateTimer pour laisser l'anim se finir
                    // Ex: Si mort depuis plus de 1 seconde
                    if (a.getStateTimer() > 1.0f) {
                        iter.remove(); // Hop, poubelle
                        continue; // On passe au suivant
                    }
                }
            }

            // Mise à jour de la physique et logique
            // Si c'est un AGENT, on délègue au spécialiste de la physique
            if (e instanceof Agent) {
                com.platformer.engine.model.Agent agent = (com.platformer.engine.model.Agent) e;

                // C'est ici que tout se joue :
                collisionManager.moveWithCollisions(agent, obstacles, delta);

                // Puis on fait l'update graphique (animation states)
                agent.update(delta);
            }
            // Si c'est un objet statique
            else {
                e.update(delta);
            }
        }
        checkGameplayCollisions();
    }
    private void checkGameplayCollisions() {
        // On récupère le joueur
        Player player = null;
        for (Entity e : entities) {
            if (e instanceof Player) {
                player = (Player) e;
                break;
            }
        }
        if (player == null || player.isDead()) return;

        // On vérifie les collisions avec les autres entités
        for (Entity e : entities) {
            if (e == player) continue;

            // On s'intéresse aux interactions Player vs Ennemis (qui sont Hostiles ET Damageable)
            if (e instanceof IHostile && e instanceof IDamageable) {

                // Note: Assure-toi que Agent implémente getHitBox() ou getPolygonHitBox()
                // Ici je suppose une collision simple Rectangle pour le gameplay (plus stable)
                if (player.getHitBox().overlaps(((Agent)e).getHitBox())) {

                    Agent enemy = (Agent) e;
                    if (enemy.isDead()) continue; // On ne tape pas un cadavre

                    // --- LOGIQUE MARIO ---

                    // Condition de l'écrasement :
                    // 1. Le joueur tombe (Vitesse Y négative)
                    // 2. Le joueur est physiquement au-dessus de l'ennemi
                    boolean isFalling = player.getVelocityY() < 0;
                    boolean isAbove = player.getY() > enemy.getY() + enemy.getHeight() * 0.5f;

                    if (isFalling && isAbove) {
                        // VICTOIRE : Le joueur écrase l'ennemi
                        enemy.takeDamage(1); // L'ennemi prend 1 dégat

                        // Rebond du joueur (Hop !)
                        player.setVelocityY(10f);
                        player.setOnGround(false);

                        System.out.println("Ennemi écrasé !");
                    }
                    else {
                        // DÉFAITE : Le joueur se fait toucher
                        IHostile hostile = (IHostile) e;
                        player.takeDamage(hostile.getDamage());

                        System.out.println("Aïe ! Touché par un ennemi.");
                    }
                }
            }
            else if (e instanceof IHostile && !(e instanceof Agent)) {
                if (player.getHitBox().overlaps(((Agent)e).getHitBox())) { // Adapter selon ta structure Entity
                    player.takeDamage(((IHostile)e).getDamage());
                }
            }
        }
    }

    public void render(SpriteBatch batch) {
        // 1. Dessiner la carte (Le fond)
        mapRenderer.setView(camera);
        mapRenderer.render();

        // 2. Dessiner les entités par dessus
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Entity e : entities) {
            // On cherche le renderer associé au Type de l'entité
            // Astuce : on utilise le textureKey comme clé de type ici, ou on ajoute un attribut 'type' dans Entity
            String type = e.getTextureKey(); // ex: "PlayerStart" ou "Goomba"

            if (rendererRegistry.containsKey(type)) {
                rendererRegistry.get(type).render(e,batch);
            }
        }
        batch.end();
    }

    // Méthode requise par ton code snippet
    public void registerRenderer(String entityType, EntityRenderer renderer) {
        rendererRegistry.put(entityType, renderer);
    }

    public List<Entity> getEntities() { return entities; }

    // Helpers pour le Clamping de la caméra
    public float getMapWidth() {
        int width = map.getProperties().get("width", Integer.class);
        return width; // Car 1 tuile = 1 unité largeur
    }

    public float getMapHeight() {
        int height = map.getProperties().get("height", Integer.class);
        return height; // Car 1 tuile = 1 unité hauteur
    }

    public float getUnitScale() {
        return unitScale;
    }

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
    }
}
