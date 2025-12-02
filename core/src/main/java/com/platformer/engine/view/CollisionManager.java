package com.platformer.engine.view;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.platformer.engine.model.Agent;
import com.platformer.game.model.Player;

public class CollisionManager {

    // Marge de sécurité (Epsilon)
    private static final float MARGIN = 0.01f;
    private float mapWidth = 0;
    private float mapHeight = 0;

    public void setWorldBounds(float width, float height) {
        this.mapWidth = width;
        this.mapHeight = height;
    }

    public void moveWithCollisions(Agent agent, Array<Polygon> obstacles, float delta) {
        // 1. Gravité
        float gravity = 40f;
        float velocityY = agent.getVelocityY() - gravity * delta;
        if (velocityY < -20f) velocityY = -20f;
        agent.setVelocityY(velocityY);

        // --- TRAITEMENT AXE X ---
        float oldX = agent.getX();
        float newX = oldX + agent.getVelocityX() * delta;

        agent.setX(newX);
        agent.updateHitbox();

        // ASTUCE 1 : "Lever les pieds"
        // Pour éviter que le sol ne bloque le mouvement X (frottement),
        // on lève temporairement la hitbox d'un tout petit peu (MARGIN)
        // Ainsi, on ne détecte que les vrais MURS, pas le sol sur lequel on glisse.

        agent.getPolygonHitBox().translate(0, MARGIN); // On lève

        for (Polygon o : obstacles) {
            if (Intersector.overlapConvexPolygons(agent.getPolygonHitBox(), o)) {
                // Rejet X
                agent.setX(oldX);
                agent.setVelocityX(0);
                // Important : on met à jour la hitbox pour qu'elle revienne à oldX
                agent.updateHitbox();
                // Note : updateHitbox() annule le translate(), donc il faudra le refaire si on continuait la boucle
                // Mais ici on break, donc c'est bon, la hitbox sera remise au propre à la ligne suivante.
                break;
            }
        }

        // Si on n'a pas touché de mur, la hitbox est toujours "levée".
        // On la remet à sa place réelle pour la suite.
        agent.updateHitbox();


        // --- TRAITEMENT AXE Y ---
        float oldY = agent.getY();
        float newY = oldY + agent.getVelocityY() * delta;

        agent.setY(newY);
        agent.updateHitbox();

        for (Polygon ground : obstacles) {
            if (Intersector.overlapConvexPolygons(agent.getPolygonHitBox(), ground)) {

                Rectangle wallBounds = ground.getBoundingRectangle();

                if (agent.getVelocityY() < 0) { // On tombe
                    // On se pose SUR le mur (sans epsilon ici, on veut être précis visuellement)
                    agent.setY(wallBounds.y + wallBounds.height);
                }
                else if (agent.getVelocityY() > 0) { // On cogne le plafond
                    agent.setY(wallBounds.y - agent.getHeight());
                }

                agent.setVelocityY(0);
                agent.updateHitbox();
                break;
            }
        }

        // --- GESTION ÉTAT "AU SOL" (Anti-Jitter) ---
        // ASTUCE 2 : "Le Capteur de Sol"
        // Au lieu de se fier à la collision précédente, on va "tâter" le terrain
        // juste en dessous des pieds.

        boolean isGrounded = false;

        // On descend la hitbox temporairement pour voir s'il y a du sol juste en dessous
        agent.getPolygonHitBox().translate(0, -MARGIN * 2);

        for (Polygon o : obstacles) {
            if (Intersector.overlapConvexPolygons(agent.getPolygonHitBox(), o)) {
                // Si on touche quelque chose en ayant baissé la hitbox, c'est qu'on est au sol !
                // (Condition : il faut ne pas être en train de monter)
                if (agent.getVelocityY() <= 0) {
                    isGrounded = true;
                    break;
                }
            }
        }


        // TRES IMPORTANT : On remet la hitbox à sa place normale
        agent.getPolygonHitBox().translate(0, MARGIN * 2);

        agent.setOnGround(isGrounded);

        // 1. Blocage Gauche (x < 0)
        if (agent.getX() < 0) {
            agent.setX(0);
            agent.setVelocityX(0); // On coupe l'élan
            agent.updateHitbox();
        }

        // 2. Blocage Droite (x > mapWidth - largeur_agent)
        // On vérifie que la map a bien été configurée (width > 0)
        if (mapWidth > 0 && agent.getX() + agent.getWidth() > mapWidth) {
            agent.setX(mapWidth - agent.getWidth());
            agent.setVelocityX(0);
            agent.updateHitbox();
        }

        // 3. (Optionnel) Mort en bas
        // Si le perso tombe sous la map, on peut le tuer ou le reset

        if (agent.getY() < -5f) { // -5 pour laisser une marge
            agent.die(); // Si tu as une méthode die()
        }
    }

}
