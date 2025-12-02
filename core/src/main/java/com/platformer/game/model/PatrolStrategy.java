package com.platformer.game.model;

import com.platformer.engine.model.IStrategy;
import com.platformer.engine.model.Agent;


public class PatrolStrategy implements IStrategy {

    // Paramètres (Tu pourrais les lire de Tiled si tu voulais être pro)
    private static final float DETECTION_RANGE = 5.0f; // 5 tuiles
    private static final float PATROL_RANGE = 5.0f;    // 5 tuiles d'allers-retours
    private static final float ATTACK_RANGE = 0.9f;    // Assez proche pour taper

    @Override
    public void execute(Agent agent, float deltaTime) {
        if (!(agent instanceof Enemy)) return;
        Enemy enemy = (Enemy) agent;
        Agent target = enemy.getTarget();

        // Si pas de cible ou cible morte, on patrouille bêtement
        if (target == null || target.isDead()) {
            patrol(enemy);
            return;
        }

        // Calcul de la distance avec le joueur
        float distanceToPlayer = Math.abs(enemy.getX() - target.getX());
        float directionToPlayer = Math.signum(target.getX() - enemy.getX()); // -1 (Gauche) ou 1 (Droite)

        // --- PHASE 1 : ATTAQUE (Priorité absolue) ---
        if (distanceToPlayer < ATTACK_RANGE) {
            // On est collé : On s'arrête et on tape !

            // 1. On coupe le moteur physique
            enemy.setVelocityX(0);

            // 2. On force l'état ATTACK
            // ATTENTION : On n'appelle PAS move(), sinon ça remettrait IDLE
            enemy.setState("ATTACK");

            // 3. On regarde vers le joueur
            // (Tu devras peut-être ajouter une méthode setFacingRight dans Agent pour forcer le regard sans bouger)

            return; // On ne fait rien d'autre
        }

        // --- PHASE 2 : POURSUITE (Chase) ---
        else if (distanceToPlayer < DETECTION_RANGE) {
            // On l'a vu ! On fonce dessus (sans limite de zone)
            enemy.move(directionToPlayer, 0);
        }

        // --- PHASE 3 : PATROUILLE (Calme) ---
        else {
            patrol(enemy);
        }
    }

    private void patrol(Enemy enemy) {
        // Logique de patrouille (limite de zone + murs)

        float spawnX = enemy.getSpawnX();
        float currentX = enemy.getX();

        // 1. Demi-tour si on sort de la zone de 5 tuiles
        if (currentX > spawnX + PATROL_RANGE && enemy.isFacingRight()) {
            enemy.move(-1, 0); // Retourne à gauche
        }
        else if (currentX < spawnX - PATROL_RANGE && !enemy.isFacingRight()) {
            enemy.move(1, 0);  // Retourne à droite
        }

        // 2. Demi-tour si on tape un mur (Vitesse nulle)
        else if (Math.abs(enemy.getVelocityX()) < 0.1f) {
            // Si on est bloqué, on change de direction
            float dir = enemy.isFacingRight() ? -1 : 1;
            enemy.move(dir, 0);
        }

        // 3. Sinon continue tout droit
        else {
            float dir = enemy.isFacingRight() ? 1 : -1;
            enemy.move(dir, 0);
        }
    }
}
